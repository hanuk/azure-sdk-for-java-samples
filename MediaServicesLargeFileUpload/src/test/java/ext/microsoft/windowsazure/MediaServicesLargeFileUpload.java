/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.java.ext.microsoft.windowsazure;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.*;
import com.microsoft.windowsazure.services.media.models.*;

public class MediaServicesLargeFileUpload {

	    private static MediaContract mediaService;
	    private static AssetInfo assetInfo;
	    private static AccessPolicyInfo accessPolicyInfo;
	    private static LocatorInfo locatorInfo;
	    private static WritableBlobContainerContract blobWriter;	    

	    public static void main(String[] args) 
	    {
	        try 
	        {
	            // Set up the MediaContract object to call into the media services.
	            initialization();  
	            
	            // Upload a local file to a media asset.
	            upload();

	            // Delete all assets. 
	            // When you want to delete the assets that have been uploaded, 
	            // comment out the calls to Upload(), Transfer(), and Download(), 
	            // and uncomment the following call to Cleanup().
	            cleanup();
	        }
	        catch (Exception e) 
	        {
	            System.out.println("Exception encountered: " + e.getStackTrace());
	        }
	    }

	    // Initialize the server context to get programmatic access to the Media Services programming objects.
	    // The media services URI, OAuth URI and scope can be used exactly as shown.
	    // Substitute your media service account name and access key for the clientId and clientSecret variables.
	    // You can obtain your media service account name and access key from the Media Services section
	    // of the Windows Azure Management portal, https://manage.windowsazure.com.
	    private static void initialization() 
	    {
	        String mediaServiceUri = "https://wamsbluclus001rest-hs.cloudapp.net/";	                                
	        String oAuthUri = "https://wamsprodglobal001acs.accesscontrol.windows.net/v2/OAuth2-13";
	        String clientId = "Your Account";  // Use your media service account name.
	        String clientSecret = "Your Secret"; // Use your media service access key. 
	        String scope = "urn:WindowsAzureMediaServices";

	        // Specify the configuration values to use with the MediaContract object.
	        Configuration configuration = MediaConfiguration
	                .configureWithOAuthAuthentication(mediaServiceUri, oAuthUri, clientId, clientSecret, scope);

	        // Create the MediaContract object using the specified configuration.
	        mediaService = MediaService.create(configuration);
	    }

	    // Upload a media file to your Media Services account.
	    // This code creates an asset, an access policy (using Write access) and a locator, 
	    // and uses those objects to upload a local file to the asset.
	    private static void upload() throws ServiceException, FileNotFoundException, NoSuchAlgorithmException, Exception 
	    {
	        // Create an asset.
	        assetInfo = mediaService.create(Asset.create().setAlternateId("altId").setOptions(AssetOption.StorageEncrypted));
	        System.out.println("Created asset with id: " + assetInfo.getId());

	        // Create an access policy that provides Write access for 15 minutes.
	        accessPolicyInfo = mediaService.create(AccessPolicy.create("uploadAccessPolicy", 15.0, EnumSet.of(AccessPolicyPermission.WRITE)));
	        System.out.println("Created access policy with id: "
	                + accessPolicyInfo.getId());

	        // Create a locator using the access policy and asset.
	        // This will provide the location information needed to add files to the asset.
	        locatorInfo = mediaService.create(Locator.create(accessPolicyInfo.getId(),
	                assetInfo.getId(), LocatorType.SAS));

	        // Create the blob writer using the locator.
	        blobWriter = mediaService.createBlobWriter(locatorInfo);

	        // The name of the file as it will exist in your Media Services account.
	        String fileName = "Large.MOV";  

	        File mediaFile = new File("C:/Sample/Large.MOV");
	        
	        // The local file that will be uploaded to your Media Services account.
	        InputStream mediaFileInputStream = new FileInputStream(mediaFile); 
	        
	        String blobName = fileName;
	        
	        // Upload the local file to the asset.
	        blobWriter.createBlockBlob(fileName, null);
	        
	        String blockId;
	        byte[] buffer = new byte[1024000];
	        BlockList blockList = new BlockList();
	        int bytesRead;
	        
	        ByteArrayInputStream byteArrayInputStream;
	        while ((bytesRead = mediaFileInputStream.read(buffer)) > 0) 
	        {
	        	blockId = UUID.randomUUID().toString();
	        	byteArrayInputStream = new ByteArrayInputStream(buffer, 0, bytesRead);
	        	blobWriter.createBlobBlock(blobName, blockId, byteArrayInputStream);
	        	blockList.addUncommittedEntry(blockId);
	        }

	        blobWriter.commitBlobBlocks(blobName, blockList);
	        
	        // Inform Media Services about the uploaded files.
	        mediaService.action(AssetFile.createFileInfos(assetInfo.getId()));
	    }
	    
	    // Remove all assets from your Media Services account.
	    // You could instead remove assets by name or ID, etc., but for 
	    // simplicity this example removes all of them.
	    private static void cleanup() throws ServiceException 
	    {
	        // Retrieve a list of all assets.
	        List<AssetInfo> assets = mediaService.list(Asset.list());

	        // Iterate through the list, deleting each asset.
	        for (AssetInfo asset: assets)
	        {
	            mediaService.delete(Asset.delete(asset.getId()));
	        }
	    }

	}
