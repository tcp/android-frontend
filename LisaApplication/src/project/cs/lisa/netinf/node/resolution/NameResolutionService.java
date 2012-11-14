/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
package project.cs.lisa.netinf.node.resolution;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.node.resolution.ResolutionService;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.lisa.exceptions.InvalidResponseException;
import project.cs.lisa.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.lisa.netinf.common.datamodel.SailDefinedLabelName;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A resolution service implementation that uses the HTTP convergence layer to a specific NRS.
 * @author Linus Sunde
 * @author Harold Martinez
 *
 */
public class NameResolutionService
        extends AbstractResolutionServiceWithoutId
        implements ResolutionService {

	/** Debug tag. **/
	public static final String TAG = "NameResolutionService";
	/** Message ID random value max. **/
	public static final int MSG_ID_MAX = 100000000;

	/** NRS IP address. **/
	private String mHost;
	/** NRS port. **/
	private int mPort;
	/** HTTP connection timeout. **/
	private static final int TIMEOUT = 5000;
	/** Implementation of DatamodelFactory, used to create and edit InformationObjects etc. **/
	private final DatamodelFactory mDatamodelFactory;
	/** Random number generator used to create message IDs. **/
	private final Random mRandomGenerator = new Random();
	/** HTTP Client. **/
	private HttpClient mClient;

	/**
	 * Creates a new Name Resolution Service that communicates with a specific NRS.
	 * @param host                 The NRS IP Address
	 * @param port                 The NRS Port
	 * @param datamodelFactory     Creates different objects necessary in the NetInf model
	 */
	@Inject
	public NameResolutionService(
	        @Named("nrs.http.host") String host,
	        @Named("nrs.http.port") int port,
	        DatamodelFactory datamodelFactory) {
	    // Setup HTTP client
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        mClient = new DefaultHttpClient(params);

        mHost = host;
        mPort = port;
        mDatamodelFactory = datamodelFactory;
	}

	@Override
	public void delete(Identifier arg0) {
	    // TODO Auto-generated method stub
	}

	@Override
	public String describe() {
		return "backend NRS";
	}

	/**
	 * Gets the hash algorithm from an identifier.
	 * @param identifier   The identifier
	 * @return             The hash algorithm
	 */
	private String getHashAlg(Identifier identifier) {
	    Log.d(TAG, "getHashAlg()");
	    String hashAlg = identifier.getIdentifierLabel(
	            SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
	    Log.d(TAG, "hashAlg = " + hashAlg);
	    return hashAlg;
	}

	/**
     * Gets the hash from an identifier.
     * @param identifier   The identifier
     * @return             The hash
     */
	private String getHash(Identifier identifier) {
	    Log.d(TAG, "getHash()");
	    String hash = identifier.getIdentifierLabel(
                SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
	    Log.d(TAG, "hash = " + hash);
	    return hash;
	}

    /**
     * Gets the content-type from an identifier.
     * @param identifier   The identifier
     * @return             The content-type
     */
    private String getContentType(Identifier identifier) {
        Log.d(TAG, "getContentType()");
        String contentType = identifier.getIdentifierLabel(
                SailDefinedLabelName.CONTENT_TYPE.getLabelName()).getLabelValue();
        Log.d(TAG, "contentType = " + contentType);
        return contentType;
    }

    /**
     * Gets the metadata from an identifier.
     * @param identifier   The identifier
     * @return             The metadata
     */
    private String getMetadata(Identifier identifier) {
        Log.d(TAG, "getMetadata()");
        String metadata = identifier.getIdentifierLabel(
                SailDefinedLabelName.META_DATA.getLabelName()).getLabelValue();
        Log.d(TAG, "metadata = " + metadata);
        return metadata;
    }

	/**
	 * Reads the next content stream from a HTTP response, expecting it to be JSON.
	 * @param response                     The HTTP response
	 * @return                             The read JSON
	 * @throws InvalidResponseException    In case reading the JSON failed
	 */
	private String readJson(HttpResponse response) throws InvalidResponseException {
	    Log.d(TAG, "readJson()");
	    if (response == null) {
	        throw new InvalidResponseException("Response is null.");
	    } else if (response.getEntity() == null) {
            throw new InvalidResponseException("Entity is null.");
        } else if (response.getEntity().getContentType() == null) {
            throw new InvalidResponseException("Content-Type is null.");
        } else if (!response.getEntity().getContentType().getValue().equals("application/json")) {
            throw new InvalidResponseException("Content-Type is "
                    + response.getEntity().getContentType().getValue()
                    + ", expected \"application/json\"");
        }
	    try {
	        String jsonString = streamToString(response.getEntity().getContent());
	        Log.d(TAG, "jsonString = " + jsonString);
	        return jsonString;
	    } catch (IOException e)  {
	        throw new InvalidResponseException("Failed to convert stream to string.", e);
	    }
	}

	/**
	 * Converts the JSON String returned in the HTTP response into a JSONObject.
	 * @param jsonString                   The JSON String from the HTTP response
	 * @return                             The JSONObject
	 * @throws InvalidResponseException    In case the JSON String is invalid
	 */
	private JSONObject parseJson(String jsonString) throws InvalidResponseException {
	    Log.d(TAG, "parseJson()");
	    JSONObject json = (JSONObject) JSONValue.parse(jsonString);
	    if (json == null) {
	        Log.e(TAG, "Unable to parse JSON");
	        Log.e(TAG, "jsonString = " + jsonString);
	        throw new InvalidResponseException("Unable to parse JSON.");
	    }
        Log.d(TAG, "json = " + json.toJSONString());
        return json;
	}

	/**
	 * If the JSON contains a content-type, extract it and set the content-type of the identifier.
	 * @param identifier       The identifier
	 * @param json             The JSON
	 */
	private void addContentType(Identifier identifier, JSONObject json) {
	    Log.d(TAG, "addContentType()");

	    // Check that the content-type is a string
	    Object object = json.get("ct");
	    Log.d(TAG, "object = " + object);
	    if (!(object instanceof String)) {
	        Log.d(TAG, "Content-Type NOT added.");
	        return;
	    }
	    String contentType = (String) object;
	    Log.d(TAG, "contentType = " + contentType);

	    // Add the content-type
    	IdentifierLabel label = mDatamodelFactory.createIdentifierLabel();
        label.setLabelName(SailDefinedLabelName.CONTENT_TYPE.getLabelName());
        label.setLabelValue(contentType);
        identifier.addIdentifierLabel(label);
	    Log.d(TAG, "Content-Type added.");
	}

    /**
     * If the JSON contains metadata, extract it and set the metadata of the identifier.
     * @param identifier       The identifier
     * @param json             The JSON
     */
	private void addMetadata(Identifier identifier, JSONObject json) {
	    Log.d(TAG, "addMetadata()");

	    // Check that the metadata is an JSONObject
	    Object object = json.get("metadata");
	    Log.d(TAG, "object = " + object);
	    if (!(object instanceof JSONObject)) {
	        Log.d(TAG, "Metadata NOT added.");
	    }
	    JSONObject metadata = (JSONObject) object;
        Log.d(TAG, "metadata = " + metadata.toJSONString());

        // Add the metadata
        IdentifierLabel label = mDatamodelFactory.createIdentifierLabel();
        label.setLabelName(SailDefinedLabelName.META_DATA.getLabelName());
        label.setLabelValue(metadata.toJSONString());
        identifier.addIdentifierLabel(label);
        Log.d(TAG, "Metadata added.");
	}

    /**
     * If the JSON contains locators, extract them and add them to the InformationObject.
     * @param io               The InformationObject
     * @param json             The JSON
     */
	private void addLocators(InformationObject io, JSONObject json) {
	    Log.d(TAG, "addLocators()");
	    JSONArray locators = (JSONArray) json.get("loc");

    	for (Object locator : locators) {

            String loc = (String) locator;
            Log.d(TAG, "loc = " + loc);
            String locWithoutScheme = loc.split("://")[1];
            Log.d(TAG, "locWithoutScheme = " + locWithoutScheme);

            Attribute newLocator = mDatamodelFactory.createAttribute();
            newLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            newLocator.setIdentification(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
            newLocator.setValue(locWithoutScheme);

            io.addAttribute(newLocator);
    	}
    }

	/**
	 * Create an InformationObject given an identifier and the HTTP response to the NetInf GET.
	 * @param identifier
	 *     The Identifier used for the NetInf GET
	 * @param response
	 *     The HTTP response
	 * @return
	 *     The InformationObject created from the identifier and HTTP response
	 * @throws InvalidResponseException
	 *     In case the HTTP response doesn't have information needed to create the InformationObject
	 */
    private InformationObject handleResponse(Identifier identifier, HttpResponse response)
            throws InvalidResponseException {
        Log.d(TAG, "handleResponse()");

        int statusCode = response.getStatusLine().getStatusCode();
        Log.d(TAG, "statusCode = " + statusCode);
        switch (statusCode) {
            case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
                // Just locators
                InformationObject io = mDatamodelFactory.createInformationObject();
                io.setIdentifier(identifier);
                String jsonString = readJson(response);
                JSONObject json = parseJson(jsonString);
                addContentType(identifier, json);
                addMetadata(identifier, json);
                addLocators(io, json);
                return io;
            case HttpStatus.SC_OK:
                // Locators and actual file
                throw new InvalidResponseException("Get response with data not handled yet.");
            default:
                // Something unhandled
                throw new InvalidResponseException("Unexpected Response Code = " + statusCode);
        }
	}


	/**
	 * Performs a NetInf GET request using the HTTP convergence layer.
	 * @param identifier       Identifier describing the InformationObject to get
	 * @return                 The InformationObject resulting from the NetInf GET
	 *                         or null if the get failed.
	 */
    @Override
	public InformationObject get(Identifier identifier) {
		Log.d(TAG, "get()");
		try {
		    // Create NetInf GET request
		    Log.d(TAG, "Creating HTTP POST");
		    String uri = "ni:///" + getHashAlg(identifier) + ";" + getHash(identifier);
		    Log.d(TAG, "uri = " + uri);
		    HttpPost getRequest = createGet(uri);

		    // Execute NetInf GET request
		    Log.d(TAG, "Executing HTTP POST");
		    HttpResponse response = mClient.execute(getRequest);

		    // Print all response headers
		    Log.d(TAG, "HTTP POST Response Headers:");
	        for (Header header : response.getAllHeaders()) {
	            Log.d(TAG, "\t" + header.getName() + " = " + header.getValue());
	        }

	        // Handle the response
	        Log.d(TAG, "Handling HTTP POST Response");
	        InformationObject io = handleResponse(identifier, response);
	        Log.d(TAG, "get() succeeded. Returning InformationObject");
	        return io;

		} catch (InvalidResponseException e) {
		    Log.e(TAG, e.getMessage());
		} catch (UnsupportedEncodingException e) {
		    Log.e(TAG, e.getMessage());
		} catch (IOException e) {
		    Log.e(TAG, e.getMessage());
		}
		Log.e(TAG, "get() failed. Returning null");
		return null;
	}

	@Override
	public List<Identifier> getAllVersions(Identifier arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(InformationObject io) {
	    Log.d(TAG, "put()");

		// Extracting values from IO's identifier
		String hashAlg    = getHashAlg(io.getIdentifier());
		String hash       = getHash(io.getIdentifier());
		String ct         = getContentType(io.getIdentifier());
		String meta       = getMetadata(io.getIdentifier());

		// Get one Bluetooth locator
		Attribute bluetoothMacAttribute =
		        io.getSingleAttribute(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
		String bluetoothMac = null;
        if (bluetoothMacAttribute != null) {
            bluetoothMac = bluetoothMacAttribute.getValueRaw();
            bluetoothMac = bluetoothMac.substring(bluetoothMac.indexOf(":") + 1);
            Log.d(TAG, "bluetoothMac = " + bluetoothMac);
        }

        try {
            Log.d(TAG, "Creating HTTP POST");
            HttpPost post = createPublish(hashAlg, hash, ct, bluetoothMac, meta);
            Log.d(TAG, "Executing HTTP POST to " + post.getURI());
            HttpResponse response = mClient.execute(post);
            Log.d(TAG, "statusCode = "
                    + Integer.toString(response.getStatusLine().getStatusCode()));
            Log.d(TAG, "content = " + streamToString(response.getEntity().getContent()));

        } catch (UnsupportedEncodingException e) {
            throw new NetInfResolutionException("Encoding not supported", e);
        } catch (IOException e) {
            throw new NetInfResolutionException("Unable to connect to NRS", e);
        }
	}

	/**
	 * Creates an HTTP POST representation of a NetInf PUBLISH message.
	 * @param hashAlg          the used hash algorithm
	 * @param hash             the resulting hash
	 * @param contentType      the MIME content type of the file
	 * @param bluetoothMac     the Bluetooth MAC Address of the publishing device
	 * @param meta             A string with JSON formatted meta-data related to the file
	 * @return                 A HttpPost representing the NetInf PUBLISH message
	 * @throws UnsupportedEncodingException    In case the encoding is not supported
	 */
    private HttpPost createPublish(
            String hashAlg, String hash, String contentType, String bluetoothMac, String meta)
            throws UnsupportedEncodingException {
        Log.d(TAG, "createPublish()");

	    HttpPost post = new HttpPost(mHost + ":" + mPort + "/netinfproto/publish");

	    MultipartEntity entity = new MultipartEntity();

		StringBody uri = new StringBody("ni:///" + hashAlg + ";" + hash + "?ct=" + contentType);
		entity.addPart("URI", uri);

		StringBody msgid =
		        new StringBody(Integer.toString(mRandomGenerator.nextInt(MSG_ID_MAX)));
		entity.addPart("msgid", msgid);

		if (bluetoothMac != null) {
            StringBody l = new StringBody("nimacbt://" + bluetoothMac);
            entity.addPart("loc1", l);
        }

		if (meta != null) {
		    StringBody ext = new StringBody(meta.toString());
		    entity.addPart("ext", ext);
		}

		StringBody rform = new StringBody("json");
		entity.addPart("rform", rform);

		try {
            entity.writeTo(System.out);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write MultipartEntity to System.out");
        }

		post.setEntity(entity);
		return post;
	}

	/**
	 * Creates an HTTP Post request to get an IO from the NRS.
	 * @param uri                              the NetInf format URI for getting IOs
	 * @return                                 The HTTP Post request
	 * @throws UnsupportedEncodingException    In case UTF-8 is not supported
	 */
	private HttpPost createGet(String uri) throws UnsupportedEncodingException {

		HttpPost post = new HttpPost(mHost + ":" + mPort + "/netinfproto/get");

		String msgid = Integer.toString(mRandomGenerator.nextInt(MSG_ID_MAX));
		String ext = "no extension";

		String completeUri = "URI=" + uri + "&msgid=" + msgid  + "&ext=" + ext;

		String encodeUrl = null;

		encodeUrl = URLEncoder.encode(completeUri, "UTF-8");

		HttpEntity newEntity =
	            new InputStreamEntity(fromString(encodeUrl), encodeUrl.getBytes().length);

		post.addHeader("Content-Type", "application/x-www-form-urlencoded");
		post.setEntity(newEntity);

		return post;
	}

	@Override
	protected ResolutionServiceIdentityObject createIdentityObject() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Converts an InputStream into a String.
     * TODO Move to Util class, probably use the better commented version from NetInfRequest
     * @param input A input stream
     * @return String representation of the input stream
     */
	private String streamToString(InputStream input) {
        try {
            return new Scanner(input).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

	/**
	 * Converts a string to a type ByteArrayInputStream.
	 *
	 * @param str string to be converted
	 *
	 * @return ByteArrayInputStream
	 */
	private static InputStream fromString(String str) {
		byte[] bytes = str.getBytes();
		return new ByteArrayInputStream(bytes);
	}
}
