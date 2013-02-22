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
import java.util.Random;
import java.util.UUID;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.media.*;
import com.microsoft.windowsazure.services.media.models.*;

public class MediaServicesEncryptionSample {

	    private static MediaContract mediaService;
	    private static AssetInfo asset;
	    private static AccessPolicyInfo accessPolicy;
	    private static LocatorInfo locator;
	    private static WritableBlobContainerContract uploader;	    
	    private static byte[] AesKey;

	    public static void main(String[] args) 
	    {
	        try 
	        {
	            // Set up the MediaContract object to call into the media services.
	            initialization();  
	            
	            // Upload a local file to a media asset.
	            upload();

	            // Decrypt the asset.
	            descryptAsset();

	            // Retrieve the URL of the asset's transformed output.
	            download();

	            // Delete all assets. 
	            // When you want to delete the assets that have been uploaded, 
	            // comment out the calls to Upload(), Transfer(), and Download(), 
	            // and uncomment the following call to Cleanup().
	            //cleanup();
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
	        String mediaServiceUri = "https://media.windows.net/API/";	                                
	        String oAuthUri = "https://wamsprodglobal001acs.accesscontrol.windows.net/v2/OAuth2-13";
	        String clientId = "your_client_id";  // Use your media service account name.
	        String clientSecret = "your_client_secret"; // Use your media service access key. 
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
	        asset = mediaService.create(Asset.create().setAlternateId("altId").setOptions(AssetOption.StorageEncrypted));
	        System.out.println("Created asset with id: " + asset.getId());

	        // Create an access policy that provides Write access for 15 minutes.
	        accessPolicy = mediaService.create(AccessPolicy.create("uploadAccessPolicy", 15.0, EnumSet.of(AccessPolicyPermission.WRITE)));
	        System.out.println("Created access policy with id: "
	                + accessPolicy.getId());

	        // Create a locator using the access policy and asset.
	        // This will provide the location information needed to add files to the asset.
	        locator = mediaService.create(Locator.create(accessPolicy.getId(),
	                asset.getId(), LocatorType.SAS));
	        System.out.println("Created locator with id: " + locator.getId());

	        // Create the blob writer using the locator.
	        uploader = mediaService.createBlobWriter(locator);

	        // The name of the file as it will exist in your Media Services account.
	        String fileName = "MPEG4-H264.mp4";  

	        // The local file that will be uploaded to your Media Services account.
	        InputStream mpeg4H264InputStream = new FileInputStream(new File("c:/media/MPEG4-H264.mp4")); 
	        
	        // encrypt the InputStream
	        InputStream encryptedContent = encryptInputStreame(mpeg4H264InputStream);	       
	       
	        // make content key
	        String contentKeyId = createContentKeyId();
	    	
	        // link the content key with the asset.
	        mediaService.action(Asset.linkContentKey(asset.getId(), contentKeyId)); 

	        // Upload the local file to the asset.
	        uploader.createBlockBlob(fileName, encryptedContent);

	        // Inform Media Services about the uploaded files.
	        mediaService.action(AssetFile.createFileInfos(asset.getId()));
	    }
	    
	    // Encrypts a stream of data using AES with a randomly generated key.
	    private static InputStream encryptInputStreame(InputStream inputStream) throws Exception {	        
	    	// Media Services requires 256-bit (32-byte) keys and
	        // 128-bit (16-byte) initialization vectors (IV) for AES encryption,
	        // and also requires that only the first 8 bytes of the IV is filled.
	        Random random = new Random();
	        AesKey = new byte[32];
	        random.nextBytes(AesKey);
	        byte[] effectiveIv = new byte[8];
	        random.nextBytes(effectiveIv);
	        byte[] iv = new byte[16];
	        System.arraycopy(effectiveIv, 0, iv, 0, effectiveIv.length); 
	        
	        InputStream encryptedInputStream = EncryptionHelper.encryptFile(inputStream, AesKey, iv);
	        System.out.println("EncryptInputStream done");
	        return encryptedInputStream; 
	    }
	    
	    // Creates a content key by encrypting AES key using protection key provided by media service server
	    private static String createContentKeyId() throws ServiceException, Exception {
	        String protectionKeyId = mediaService.action(ProtectionKey.getProtectionKeyId(ContentKeyType.StorageEncryption));
	        String protectionKey = mediaService.action(ProtectionKey.getProtectionKey(protectionKeyId));

	        String contentKeyIdUuid = UUID.randomUUID().toString();
	        String contentKeyId = String.format("nb:kid:UUID:%s", contentKeyIdUuid);   

	        byte[] encryptedContentKey = EncryptionHelper.encryptAesKey(protectionKey, AesKey);
	        String encryptedContentKeyString = Base64.encode(encryptedContentKey);
	        String checksum = EncryptionHelper.calculateContentKeyChecksum(contentKeyIdUuid, AesKey);

	        ContentKeyInfo contentKeyInfo = mediaService.create(ContentKey
	                .create(contentKeyId, ContentKeyType.StorageEncryption, encryptedContentKeyString)
	                .setChecksum(checksum).setProtectionKeyId(protectionKeyId));
	        
	        System.out.println("createContentKeyId done");
	        return contentKeyInfo.getId();
	    }	  

	    // Create a job that contains a task to transform the asset.
	    // In this example, the asset will be transformed using the Windows Azure Media Encoder.
 	    private static void descryptAsset() throws ServiceException, InterruptedException 
	    {
	        // Use the Windows Azure Media Encoder, by specifying it by name.
	        MediaProcessorInfo mediaProcessor = mediaService.list(MediaProcessor.list().set("$filter", "Name eq 'Windows Azure Media Encoder'")).get(0);

	        // Create a task with the specified media processor, in this case to transform the original asset to the H.264 HD 720p VBR preset.
	        // Information on the various configurations can be found at
	        // http://msdn.microsoft.com/en-us/library/microsoft.expression.encoder.presets_members%28v=Expression.30%29.aspx.
	        // This example uses only one task, but others could be added.
	        Task.CreateBatchOperation task = Task.create(
	                mediaProcessor.getId(),
	                "<taskBody><inputAsset>JobInputAsset(0)</inputAsset><outputAsset>JobOutputAsset(0)</outputAsset></taskBody>")
	                .setConfiguration("H.264 HD 720p VBR").setName("MyTask");

	        // Create a job creator that specifies the asset, priority and task for the job. 
	        Job.Creator jobCreator = Job.create()
	            .setName("myJob")
	            .addInputMediaAsset(asset.getId())
	            .setPriority(2)
	            .addTaskCreator(task);

	        // Create the job within your Media Services account.
	        // Creating the job automatically schedules and runs it.
	        JobInfo jobInfo = mediaService.create(jobCreator);
	        String jobId = jobInfo.getId();
	        System.out.println("Created job with id: " + jobId);
	        // Check to see if the job has completed.
	        checkJobStatus(jobId);
	    }

	    // Gets the URI of the decrypted asset.
	    // This code an access policy (with Read access) and a locator,
	    // and uses those objects to retrieve the path.
	    // You can use the path to access the asset.
	    private static void download() throws ServiceException 
	    {
	        // Create an access policy that provides Read access for 15 minutes.
	        AccessPolicyInfo downloadAccessPolicy = mediaService.create(AccessPolicy.create("Download", 15.0, EnumSet.of(AccessPolicyPermission.READ)));

	        // Create a locator using the access policy and asset.
	        // This will provide the location information needed to access the asset.
	        LocatorInfo locatorInfo = mediaService.create(Locator.create(downloadAccessPolicy.getId(), asset.getId(), LocatorType.SAS));

	        // Iterate through the files associated with the asset.
	        for(AssetFileInfo assetFile: mediaService.list(AssetFile.list(asset.getAssetFilesLink())))
	        {
	            String file = assetFile.getName();
	            String locatorPath = locatorInfo.getPath();
	            int startOfSas = locatorPath.indexOf("?");
	            String blobPath = locatorPath + file;
	            if (startOfSas >= 0) 
	            {
	                blobPath = locatorPath.substring(0, startOfSas) + "/" + file + locatorPath.substring(startOfSas);
	            }
	            System.out.println("Path to asset file: " + blobPath);
	        }
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

	    // Helper function to check to on the status of the job.
	    private static void checkJobStatus(String jobId) throws InterruptedException, ServiceException
	    {
	        int maxRetries = 12; // Number of times to retry. Small jobs often take 2 minutes.
	        JobState jobState = null;
	        while (maxRetries > 0) 
	        {
	            Thread.sleep(10000);  // Sleep for 10 seconds, or use another interval.
	            // Determine the job state.
	            jobState = mediaService.get(Job.get(jobId)).getState();
	            System.out.println("Job state is " + jobState);

	            if (jobState == JobState.Finished || jobState == JobState.Canceled || jobState == JobState.Error) 
	            {
	                // The job is done.
	                break;
	            }
	            // The job is not done. Sleep and loop if max retries 
	            // has not been reached.
	            maxRetries--;
	        }
	    }
	}
