package sharepointapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.microsoft.schemas.sharepoint.soap.Copy;
import com.microsoft.schemas.sharepoint.soap.CopyResultCollection;
import com.microsoft.schemas.sharepoint.soap.CopySoap;
import com.microsoft.schemas.sharepoint.soap.DestinationUrlCollection;
import com.microsoft.schemas.sharepoint.soap.FieldInformationCollection;
import com.microsoft.schemas.sharepoint.soap.GetListCollectionResponse.GetListCollectionResult;
import com.microsoft.schemas.sharepoint.soap.GetListItems;
import com.microsoft.schemas.sharepoint.soap.GetListItemsResponse;
import com.microsoft.schemas.sharepoint.soap.Lists;
import com.microsoft.schemas.sharepoint.soap.ListsSoap;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author allarj3
 *
 */
public class SharePointWebController {

	private static final String BASE_TYPE_ATTRIBUTE_NAME = "BaseType";
	private static final String DEFAULT_VIEW_URL_ATTRIBUTE_NAME = "DefaultViewUrl";
	private static final String TITLE_ATTRIBUTE_NAME = "Title";
	private static final String LIST_ELEMENT_NAME = "List";
	private ListsSoap listsoapstub;
	private CopySoap copysoapstub;
	private String BasesharepointUrl = "";
	private static SharePointWebController instance = new SharePointWebController();
	private SharePointAuthenticator authenticator;
	private Connector spConnectorThread;

	private Map<String, String> defaultUrls = new HashMap<String, String>();
	private boolean isConnected = false;;

	/**
	 * 
	 */
	private SharePointWebController() {

		authenticator = new SharePointAuthenticator();
		Authenticator.setDefault(authenticator);
	}

	/**
	 * @return
	 */
	public String getBasesharepointUrl() {
		return BasesharepointUrl;
	}

	/**
	 * @return
	 */
	public static SharePointWebController getInstance() {
		return instance;
	}

	/**
	 * @author allarj3
	 *
	 */
	private class Connector extends Thread {
		private String listToLoad;

		public Connector(String listToLoad) {
			this.listToLoad = listToLoad;
		}

		@Override
		public void run() {
			isConnected = false;
			listsoapstub = getSPListSoapStub(authenticator.getDomainAndUsername(), authenticator.getPassword(),
					BasesharepointUrl);
			copysoapstub = getSPCopySoapStub(authenticator.getDomainAndUsername(), authenticator.getPassword(),
					BasesharepointUrl);
			SharePointMainController.getInstance().finishedConnectingToSite(getAllLists(), listToLoad);
			// SPListView.getInstance().getLists(listToLoad);
			spConnectorThread = null;
		}

	}

	/**
	 * @param listName
	 * @return
	 */
	public String getDefaultUrlForList(String listName) {
		if (defaultUrls.containsKey(listName) && defaultUrls.get(listName).length() > 3) {
			String defaultUrl = defaultUrls.get(listName);
			defaultUrl = defaultUrl.substring(1, defaultUrl.length() - 1);

			String[] siteUrlParts = BasesharepointUrl.split("/");
			String[] defaultUrlParts = defaultUrl.split("/");
			String currentSiteName = siteUrlParts[siteUrlParts.length - 1];
			defaultUrl = defaultUrl.replaceAll(".*" + currentSiteName + "/", "");
			defaultUrl = defaultUrl.replaceAll("/Forms/.*", "");
			defaultUrl = defaultUrl.replaceAll("/" + defaultUrlParts[defaultUrlParts.length - 1], "");

			return "/" + defaultUrl;
		} else {
			return null;
		}
	}

	/**
	 * @param url
	 */
	public void connectToUrl(String url) {
		connectToUrl(url, null);
	}

	/**
	 * @param url
	 * @param listToLoad
	 */
	public void connectToUrl(String url, String listToLoad) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		if (spConnectorThread != null && spConnectorThread.isAlive()) {
			spConnectorThread.interrupt();
		}
		BasesharepointUrl = url;
		spConnectorThread = new Connector(listToLoad);
		spConnectorThread.start();
	}

	/**
	 * @param username
	 * @param password
	 * @param url
	 * @return
	 */
	private CopySoap getSPCopySoapStub(String username, String password, String url) {

		CopySoap port = null;
		if (username != null && password != null) {
			try {
				// This is to avoid the error where it tries to find wsdl file,
				// due to hardcoded path of wsdl in your stub generated by
				// wsimport.exe
				//
				// Somehow class.getResource() did not work for me,
				// so I am using class.getClassLoader.getResource()
				//
				// URL wsdlURL = new URL(
				// getInstance().getClass().getClassLoader().getResource().toExternalForm());
				// File file = new File("./copy.wsdl");
				// Copy service = new Copy(file.toURI().toURL());
				Copy service = new Copy(this.getClass().getResource("/copy.wsdl"));
				port = service.getCopySoap();
				((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
				((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

				// To avoid, error with endpoint not supported, you need to give
				// end point url here.
				//
				URL convertedurl = convertToURLEscapingIllegalCharacters(url + "/_vti_bin/copy.asmx");
				((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
						convertedurl.toString());

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
		return port;
	}

	/**
	 * @param username
	 * @param password
	 * @param url
	 * @return
	 */
	private ListsSoap getSPListSoapStub(String username, String password, String url) {
		ListsSoap port = null;
		if (username != null && password != null) {
			try {
				// This is to avoid the error where it tries to find wsdl file,
				// due to hardcoded path of wsdl in your stub generated by
				// wsimport.exe
				//
				// Somehow class.getResource() did not work for me,
				// so I am using class.getClassLoader.getResource()
				//
				// URL wsdlURL = new URL(
				// getInstance().getClass().getClassLoader().getResource().toExternalForm());
				// File file = new File("./Lists.wsdl");
				// Lists service = new Lists(file.toURI().toURL());
				Lists service = new Lists(this.getClass().getResource("/Lists.wsdl"));
				port = service.getListsSoap();
				((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
				((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

				// To avoid, error with endpoint not supported, you need to give
				// end point url here.
				//
				URL convertedurl = convertToURLEscapingIllegalCharacters(url + "/_vti_bin/Lists.asmx");
				((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
						convertedurl.toString());

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
		return port;
	}

	/**
	 * @param filepath
	 * @param testUrl
	 * @return
	 * @throws Exception
	 */
	public boolean downloadFile(String filepath, String testUrl) throws Exception {
		testUrl = BasesharepointUrl + testUrl;
		testUrl = testUrl.replace(" ", "%20");
		Holder<Long> result = new Holder<Long>();
		Holder<FieldInformationCollection> fields = new Holder<FieldInformationCollection>();
		Holder<byte[]> stream = new Holder<byte[]>();
		copysoapstub.getItem(testUrl, result, fields, stream);
		if (stream.value != null) {

			File file = new File(filepath);
			FileOutputStream output = new FileOutputStream(file);
			output.write(stream.value);
			output.flush();
			output.close();
			System.out.println("File downloaded at: " + file.getAbsolutePath());
			return true;
		} else {
			System.out.println("Failed to download file: " + testUrl);
			return false;
		}
	}

	/**
	 * @param filepath
	 * @param testUrl
	 * @return
	 * @throws Exception
	 */
	public boolean uploadFile(String filepath, String testUrl) throws Exception {
		testUrl = BasesharepointUrl + testUrl;
		testUrl = testUrl.replace(" ", "%20");
		File file = new File(filepath);
		DestinationUrlCollection dests = new DestinationUrlCollection();
		dests.getString().add(testUrl);
		Holder<Long> copyIntoItemsResult = new Holder<Long>();
		Holder<CopyResultCollection> results = new Holder<CopyResultCollection>();
		FieldInformationCollection fields = new FieldInformationCollection();
		byte[] stream = Files.readAllBytes(file.toPath());
		copysoapstub.copyIntoItems(file.getAbsolutePath(), dests, fields, stream, copyIntoItemsResult, results);

		if (results.value.getCopyResult().size() > 0
				&& results.value.getCopyResult().get(0).getErrorMessage() == null) {
			System.out.println("File uploaded at: " + testUrl);
			return true;
		} else if (results.value.getCopyResult().size() > 0) {
			System.out.println("Failed to upload document: " + results.value.getCopyResult().get(0).getErrorMessage());
			return false;
		} else {
			System.out.println("Unable to upload document");
			return false;
		}
	}

	/**
	 * @return
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * @return
	 */
	public List<String> getAllLists() {
		try {
			defaultUrls.clear();
			GetListCollectionResult lists = listsoapstub.getListCollection();
			isConnected = true;
			return getListsFromContents(lists.getContent(), true);
		} catch (Exception e) {

			e.printStackTrace();
			isConnected = false;
			return null;
		}
	}

	/**
	 * @param listName
	 * @return
	 * @throws Exception
	 */
	public List<Object> getAllListItems(String listName) throws Exception {
		String rowLimit = "150";

		// Here are additional parameters that may be set
		String viewName = "";
		GetListItems.ViewFields viewFields = null;
		GetListItems.Query query = null;
		GetListItems.QueryOptions queryOptions = null;
		String webID = "";

		// Calling the List Web Service
		GetListItemsResponse.GetListItemsResult result = listsoapstub.getListItems(listName, viewName, query,
				viewFields, rowLimit, queryOptions, webID);

		return result.getContent();

	}

	/**
	 * @param contents
	 * @param printXML
	 * @return
	 * @throws Exception
	 */
	private List<String> getListsFromContents(List<Object> contents, boolean printXML) throws Exception {

		List<String> valuesToReturn = new ArrayList<String>();
		for (Object o : contents) {

			if (o instanceof ElementNSImpl) {
				ElementNSImpl element = (ElementNSImpl) o;
				if (printXML) {
					printElementXML(element);
				}
				findMatchingElements(valuesToReturn, element);
			}
		}
		return valuesToReturn;
	}

	/**
	 * @param element
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
	private void printElementXML(Element element)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(element.getOwnerDocument()), new StreamResult(writer));
		String output = writer.getBuffer().toString().replace("/>", "/>\n");
		System.out.println(output);
	}

	/**
	 * @param valuesToReturn
	 * @param element
	 */
	private void findMatchingElements(List<String> valuesToReturn, Node element) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node currentChild = children.item(i);
			if (currentChild.getNodeName().compareToIgnoreCase(LIST_ELEMENT_NAME) == 0) {
				Node title = currentChild.getAttributes().getNamedItem(TITLE_ATTRIBUTE_NAME);
				if (title != null) {
					valuesToReturn.add(title.getNodeValue());
				}
				Node baseType = currentChild.getAttributes().getNamedItem(BASE_TYPE_ATTRIBUTE_NAME);
				Node defaultView = currentChild.getAttributes().getNamedItem(DEFAULT_VIEW_URL_ATTRIBUTE_NAME);
				if (defaultView != null && baseType != null && !baseType.getNodeValue().equals("0")) {
					defaultUrls.put(title.getNodeValue(), defaultView.getNodeValue());
				}
			} else {
				findMatchingElements(valuesToReturn, currentChild);
			}
		}
	}

	/**
	 * @param contents
	 * @return
	 */
	public String[] getAttributeNames(List<Object> contents) {
		String[] names = null;
		for (Object o : contents) {

			if (o instanceof ElementNSImpl) {
				ElementNSImpl element = (ElementNSImpl) o;

				Node child = element.getElementsByTagName("z:row").item(0);
				if (child != null) {
					NamedNodeMap attributes = child.getAttributes();
					names = new String[attributes.getLength()];
					for (int i = 0; i < attributes.getLength(); i++) {
						names[i] = attributes.item(i).getNodeName().replace("ows", "").replace("_", "")
								.replace("FileLeafRef", "FileName (FileLeafRef)");
					}
				}
			}
		}
		return names;
	}

	/**
	 * @param contents
	 * @param columnNames
	 * @return
	 */
	public Object[][] getData(List<Object> contents, String[] columnNames) {
		Object[][] data = null;
		List<String> columns = Arrays.asList(columnNames);
		for (Object o : contents) {

			if (o instanceof ElementNSImpl) {
				ElementNSImpl element = (ElementNSImpl) o;
				NodeList children = element.getElementsByTagName("z:row");
				data = new Object[children.getLength()][];
				for (int i = 0; i < children.getLength(); i++) {
					NamedNodeMap attributes = children.item(i).getAttributes();
					data[i] = new Object[columns.size()];
					for (int j = 0; j < attributes.getLength() && j < columns.size(); j++) {
						int indexOfAttribute = columns.indexOf(attributes.item(j).getNodeName().replace("ows", "")
								.replace("_", "").replace("FileLeafRef", "FileName (FileLeafRef)"));
						if (indexOfAttribute != -1) {
							data[i][indexOfAttribute] = attributes.item(j).getNodeValue().replaceFirst(".*;#", "");
						}
					}
				}
			}
		}
		return data;
	}

	/**
	 * @param string
	 * @return
	 */
	public URL convertToURLEscapingIllegalCharacters(String string) {
		try {
			String decodedURL = URLDecoder.decode(string, "UTF-8");
			URL url = new URL(decodedURL);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), url.getRef());
			return uri.toURL();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
