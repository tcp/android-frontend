/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.android.transferdispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netinf.android.common.datamodel.SailDefinedLabelName;
import netinf.android.transferdispatcher.providers.BluetoothProvider;
import netinf.android.transferdispatcher.providers.ByteArrayProvider;
import netinf.android.transferdispatcher.providers.HttpProvider;
import netinf.android.transferdispatcher.providers.TcpProvider;
import netinf.common.datamodel.InformationObject;
import netinf.common.log.demo.DemoLevel;
import netinf.node.transferdispatcher.streamprovider.NetInfNoStreamProviderFoundException;
import netinf.node.transferdispatcher.streamprovider.StreamProvider;

import org.apache.log4j.Logger;

import android.bluetooth.BluetoothAdapter;

/**
 * The TransferDispatcher. Responsible for Providing stream to IOs/Files.
 * 
 * @author PG NetInf 3, University of Paderborn.
 * @pat.name Singleton.
 * @pat.task Forces that only one instance of this class exists.
 */
public final class TransferDispatcher {

   private static final Logger LOG = Logger.getLogger(TransferDispatcher.class);
   private static final String NCS_SERVER_LOCATOR      = "nihttp://192.36.165.136:8183";
   private List<StreamProvider> streamProviders;
   private List<ByteArrayProvider> byteArrayProviders;
   private static TransferDispatcher instance;
   


   private String bluetoothMacAddress = null;
   
   /**
    * Constructor.
    */
   private TransferDispatcher() {
      addByetArrayProviders();
      
   	BluetoothAdapter bluetoothDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
   	if ((bluetoothDefaultAdapter != null) && (bluetoothDefaultAdapter.isEnabled())){
   		bluetoothMacAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
   	}
   }
   
   /**
    * Adds available ByetArrayProviders to the TD.
    */
   private void addByetArrayProviders() {
	   byteArrayProviders = new ArrayList<ByteArrayProvider>();
	   byteArrayProviders.add(new BluetoothProvider());
	   byteArrayProviders.add(new TcpProvider());
	   byteArrayProviders.add(new HttpProvider());
   }

   /**
    * Provides the singleton instance of a TransferDispatcher.
    * 
    * @return The TransferDispatcher.
    */
   public static synchronized TransferDispatcher getInstance() {
      if (instance == null) {
         instance = new TransferDispatcher();
      }
      return instance;
   }

      
   /**
    * Given a locator and a file hash, this method provides the byte array corresponding
    * to the hash.
    * 
    * @param locator
    *           The locator from where the file should be fetched
    * @param hash
    *           The hash of the file that will be fetched
    * @return The byte array corresponding to the hash of the file obtained from the address
    *         specified in the locator.
    */
   public byte[] getByteArray(String locator, String hash) throws IOException, NetInfNoStreamProviderFoundException {
     	  
 	  if(!locator.contains(bluetoothMacAddress)){ 
		  LOG.log(DemoLevel.DEMO, "(TD ) Getting Transfer-Stream from: " + locator);
	      ByteArrayProvider mProvider = getByteArrayProvider(locator);
	      if(mProvider!=null)
	    	  return mProvider.getByteArray(locator,hash);
	      else
	    	  return null;
 	  }
 	  else
 		  return null;
   }
   
   
   /**
    * Provides the stream by a given DO.
    * 
    * @param dataObj
    *           The DO.
    * @return Stream to the underlying BO.
    * @throws IOException
    */
   public byte[] getByteArray(InformationObject myIO) throws IOException {
      
	   byte[] resultArray;
	   String hash    = myIO.getIdentifier().getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
	   LOG.log(DemoLevel.DEMO, "(TD ) Getting Transfer-Stream from IO: " + myIO);

      LOG.info("(TransferDispatcher ) Try to get stream over normal locators");
      LOG.log(DemoLevel.DEMO, "(TD ) Try to get stream over normal locators");
      LocatorSelector locSel = new LocatorSelector(myIO);
      while (locSel.hasNext()) {
    	  
         try {       	 
        	resultArray = this.getByteArray(locSel.next(), hash);
            if (resultArray!=null)
            	return resultArray;
         } catch (NetInfNoStreamProviderFoundException e) {
            LOG.warn("(TransferDispatcher ) NoStreamProviderFoundException: " + e.getMessage());
         } catch (IOException e) {
            LOG.warn("(TransferDispatcher ) IOException: " + e.getMessage());
         }	  
      }

      throw new IOException("Stream could not be provided");
   }
   
   
   
   /**
    * Provides the appropriate ByteArrayProvider
    * 
    * @param url
    *           The locator from where the file will be fetched
    * @return The specific ByteArrayProvider.
    */
   ByteArrayProvider getByteArrayProvider(String locator)  {
      for (ByteArrayProvider bp : byteArrayProviders) {
         if (bp.canHandle(locator)) {
            LOG.log(DemoLevel.DEMO, "(TD ) Using " + bp.describe());
            return bp;
         }
      }
      return null;
   }
   
   /**
    * Provides the appropriate StreamProvider.
    * 
    * @param url
    *           The URL to which a stream shall be povided.
    * @return The specific StreamProvider.
    * @throws NetInfNoStreamProviderFoundException
    *            If no provider could be found.
    */
   StreamProvider getStreamProvider(String url) throws NetInfNoStreamProviderFoundException {
      for (StreamProvider sp : streamProviders) {
         if (sp.canHandle(url)) {
            LOG.log(DemoLevel.DEMO, "(TD ) Using " + sp.describe());
            return sp;
         }
      }
      throw new NetInfNoStreamProviderFoundException(url + " not supported");
   }

   
   public boolean cacheContentInNCS(String hashAlg,String hashContent){
	   
	   String locator = NCS_SERVER_LOCATOR;
       ByteArrayProvider mProvider = getByteArrayProvider(locator);
       if(mProvider!=null&& mProvider instanceof HttpProvider)
    	 return ((HttpProvider) mProvider).cacheContent(hashAlg, hashContent);
	   
       return false;
	   
   }
}