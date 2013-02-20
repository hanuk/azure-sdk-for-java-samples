package PortalSnippets;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.microsoft.windowsazure.services.media.models.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.EnumSet;

/**
 * Demonstrating use of Windows Azure Media Services from Java.
 * This sample works on Java SE 6 and up.
 */
public class App {
    public static void main(String[] args) throws ServiceException, FileNotFoundException, URISyntaxException {
        // Create a Java console app
        // Add Maven dependency on microsoft-windowsazure-api, version 0.4.0 or newer

        // ** UPLOAD **

        Configuration config = MediaConfiguration.configureWithOAuthAuthentication(
                "https://media.windows.net/API/",
                "https://wamsprodglobal001acs.accesscontrol.windows.net/v2/OAuth2-13",
                "<Account name from portal>",
                "<Key from portal>",
                "urn:WindowsAzureMediaServices");

        MediaContract service = MediaService.create(config);

        File inputFilePath = new File("D:/Dev/PortalSnippets/media/bing_social_search.mp4");
        String assetName = inputFilePath.getName().substring(0, inputFilePath.getName().lastIndexOf('.'));

        AssetInfo inputAsset = service.create(Asset.create()
                .setName(assetName)
                .setOptions(AssetOption.None));

        AccessPolicyInfo writable = service.create(
                AccessPolicy.create("writable", 10, EnumSet.of(AccessPolicyPermission.WRITE)));

        LocatorInfo assetBlobStorageLocator = service.create(
                Locator.create(writable.getId(), inputAsset.getId(), LocatorType.SAS));

        WritableBlobContainerContract writer = service.createBlobWriter(assetBlobStorageLocator);

        writer.createBlockBlob(inputFilePath.getName(), new FileInputStream(inputFilePath));

        service.action(AssetFile.createFileInfos(inputAsset.getId()));

        AssetInfo preparedAsset;

        // ** END UPLOAD **

        if (false) {
            // ** ENCODE HTML 5 **

            // Create a Java console app
            // Add Maven dependency on microsoft-windowsazure-api, version 0.4.0 or newer
            //
            // TODO: Uncomment the following if you are not using the previous snippets
            // Configuration config = MediaConfiguration.configureWithOAuthAuthentication(
            //         "https://media.windows.net/API/",
            //         "https://wamsprodglobal001acs.accesscontrol.windows.net/v2/OAuth2-13",
            //         "<Account name from portal>",
            //         "<Key from portal>",
            //         "urn:WindowsAzureMediaServices");
            //
            // MediaContract service = MediaService.create(config);

            // TODO: Replace with a retrieved asset if you are not using the previous snippets
            AssetInfo assetToEncode = inputAsset;

            //Preset reference documentation: http://msdn.microsoft.com/en-us/library/windowsazure/jj129582.aspx
            String encodingPreset = "H264 Broadband 720p";

            ListResult<MediaProcessorInfo> processors = service.list(MediaProcessor.list());
            MediaProcessorInfo latestWameMediaProcessor = null;

            for (MediaProcessorInfo info : processors) {
                if (info.getName().equals("Windows Azure Media Encoder")) {
                    if (latestWameMediaProcessor == null ||
                            info.getVersion().compareTo(latestWameMediaProcessor.getVersion()) > 0) {
                        latestWameMediaProcessor = info;
                    }
                }
            }

            String outputAssetName = assetToEncode.getName() + " as " + encodingPreset;

            JobInfo job = service.create(Job.create()
                    .setName("Encoding " + assetToEncode.getName() + " to " + encodingPreset)
                    .addInputMediaAsset(assetToEncode.getId())
                    .addTaskCreator(Task.create(latestWameMediaProcessor.getId(),
                            "<taskBody>" +
                                    "<inputAsset>JobInputAsset(0)</inputAsset>" +
                                    "<outputAsset assetName='" + outputAssetName + "'>JobOutputAsset(0)</outputAsset>" +
                                    "</taskBody>")
                            .setConfiguration(encodingPreset)
                            .setOptions(TaskOption.None)
                            .setName("Encoding")
                    )
            );

            while (true) {
                JobInfo currentJob = service.get(Job.get(job.getId()));
                JobState state = currentJob.getState();
                if (state == JobState.Finished || state == JobState.Canceled ||
                        state == JobState.Error) {
                    break;
                }
            }

            job = service.get(Job.get(job.getId()));
            if (job.getState() == JobState.Error) {
                ListResult<TaskInfo> tasks = service.list(Task.list(job.getTasksLink()));
                for (TaskInfo task : tasks) {
                    System.out.println("Task status for " + task.getName());
                    for (ErrorDetail detail : task.getErrorDetails()) {
                        System.out.println(detail.getMessage());
                    }
                }
            }
            ListResult<AssetInfo> outputAssets = service.list(Asset.list(job.getOutputAssetsLink()));
            preparedAsset = outputAssets.get(0);

            // ** END ENCODE HTML 5 **
        }

        if (false) {
            // ** ENCODE PC MAC **

            // Create a Java console app
            // Add Maven dependency on microsoft-windowsazure-api, version 0.4.0 or newer
            //
            // TODO: Uncomment the following if you are not using the previous snippets
            // Configuration config = MediaConfiguration.configureWithOAuthAuthentication(
            //         "https://media.windows.net/API/",
            //         "https://wamsprodglobal001acs.accesscontrol.windows.net/v2/OAuth2-13",
            //         "<Account name from portal>",
            //         "<Key from portal>",
            //         "urn:WindowsAzureMediaServices");
            //
            // MediaContract service = MediaService.create(config);

            // TODO: Replace with a retrieved asset if you are not using the previous snippets
            AssetInfo assetToEncode = inputAsset;

            //Preset reference documentation: http://msdn.microsoft.com/en-us/library/windowsazure/jj129582.aspx
            String encodingPreset = "H264 Smooth Streaming 720p";

            ListResult<MediaProcessorInfo> processors = service.list(MediaProcessor.list());
            MediaProcessorInfo latestWameMediaProcessor = null;

            for (MediaProcessorInfo info : processors) {
                if (info.getName().equals("Windows Azure Media Encoder")) {
                    if (latestWameMediaProcessor == null ||
                            info.getVersion().compareTo(latestWameMediaProcessor.getVersion()) > 0) {
                        latestWameMediaProcessor = info;
                    }
                }
            }

            String outputAssetName = assetToEncode.getName() + " as " + encodingPreset;

            JobInfo job = service.create(Job.create()
                    .setName("Encoding " + assetToEncode.getName() + " to " + encodingPreset)
                    .addInputMediaAsset(assetToEncode.getId())
                    .addTaskCreator(Task.create(latestWameMediaProcessor.getId(),
                            "<taskBody>" +
                                    "<inputAsset>JobInputAsset(0)</inputAsset>" +
                                    "<outputAsset assetName='" + outputAssetName + "'>JobOutputAsset(0)</outputAsset>" +
                                    "</taskBody>")
                            .setConfiguration(encodingPreset)
                            .setOptions(TaskOption.None)
                            .setName("Encoding")
                    )
            );

            while (true) {
                JobInfo currentJob = service.get(Job.get(job.getId()));
                JobState state = currentJob.getState();
                if (state == JobState.Finished || state == JobState.Canceled ||
                        state == JobState.Error) {
                    break;
                }
            }

            job = service.get(Job.get(job.getId()));
            ListResult<AssetInfo> outputAssets = service.list(Asset.list(job.getOutputAssetsLink()));
            preparedAsset = outputAssets.get(0);

            // ** END ENCODE PC MAC **
        }

        if (true) {
            // ** ENCODE iOS **

            // Create a Java console app
            // Add Maven dependency on microsoft-windowsazure-api, version 0.4.0 or newer
            //
            // TODO: Uncomment the following if you are not using the previous snippets
            // Configuration config = MediaConfiguration.configureWithOAuthAuthentication(
            //         "https://media.windows.net/API/",
            //         "https://wamsprodglobal001acs.accesscontrol.windows.net/v2/OAuth2-13",
            //         "<Account name from portal>",
            //         "<Key from portal>",
            //         "urn:WindowsAzureMediaServices");
            //
            // MediaContract service = MediaService.create(config);

            // TODO: Replace with a retrieved asset if you are not using the previous snippets
            AssetInfo assetToEncode = inputAsset;

            //Preset reference documentation: http://msdn.microsoft.com/en-us/library/windowsazure/jj129582.aspx
            String encodingPreset = "H264 Smooth Streaming 720p";

            ListResult<MediaProcessorInfo> processors = service.list(MediaProcessor.list());
            MediaProcessorInfo latestWameMediaProcessor = null;
            MediaProcessorInfo latestPackagerMediaProcessor = null;

            for (MediaProcessorInfo info : processors) {
                if (info.getName().equals("Windows Azure Media Encoder")) {
                    if (latestWameMediaProcessor == null ||
                            info.getVersion().compareTo(latestWameMediaProcessor.getVersion()) > 0) {
                        latestWameMediaProcessor = info;
                    }
                } else if (info.getName().equals("Windows Azure Media Packager")) {
                    if (latestPackagerMediaProcessor == null ||
                            info.getVersion().compareTo(latestPackagerMediaProcessor.getVersion()) > 0) {
                        latestPackagerMediaProcessor = info;
                    }
                }
            }

            String outputAssetName = assetToEncode.getName() + " encoded and packaged to HLS";
            String packagingToSmoothConfig = "<taskDefinition xmlns='http://schemas.microsoft.com/iis/media/v4/TM/TaskDefinition#'><name>Smooth Streams to Apple HTTP Live Streams</name><description xml:lang='en'/><inputDirectory/><outputFolder/><properties namespace='http://schemas.microsoft.com/iis/media/AppleHTTP#' prefix='hls'><property name='maxbitrate' value='10000000' /><property name='segment' value='10' /><property name='encrypt' value='false' /><property name='pid' value='' /><property name='codecs' value='false' /><property name='backwardcompatible' value='false' /><property name='allowcaching' value='true' /><property name='passphrase' value='' /><property name='key' value='' /><property name='keyuri' value='' /><property name='overwrite' value='true' /></properties><taskCode><type>Microsoft.Web.Media.TransformManager.SmoothToHLS.SmoothToHLSTask, Microsoft.Web.Media.TransformManager.SmoothToHLS, Version=1.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35</type></taskCode></taskDefinition>";
            JobInfo job = service.create(Job.create()
                    .setName("Encoding " + assetToEncode.getName() + " to " + encodingPreset + " and Packaging to HLS")
                    .addInputMediaAsset(assetToEncode.getId())
                    .addTaskCreator(Task.create(latestWameMediaProcessor.getId(),
                            "<taskBody>" +
                                    "<inputAsset>JobInputAsset(0)</inputAsset>" +
                                    "<outputAsset>JobOutputAsset(0)</outputAsset>" +
                                    "</taskBody>")
                            .setConfiguration(encodingPreset)
                            .setOptions(TaskOption.None)
                            .setName("Encoding")
                    )
                    .addTaskCreator(Task.create(latestPackagerMediaProcessor.getId(),
                            "<taskBody>" +
                                    "<inputAsset>JobOutputAsset(0)</inputAsset>" +
                                    "<outputAsset assetName='" + outputAssetName + "'>JobOutputAsset(1)</outputAsset>" +
                                    "</taskBody>")
                            .setConfiguration(packagingToSmoothConfig)
                            .setOptions(TaskOption.None)
                            .setName("Packaging")
                    )
            );

            while (true) {
                JobInfo currentJob = service.get(Job.get(job.getId()));
                JobState state = currentJob.getState();
                if (state == JobState.Finished || state == JobState.Canceled ||
                        state == JobState.Error) {
                    break;
                }
            }

            job = service.get(Job.get(job.getId()));
            ListResult<AssetInfo> outputAssets = service.list(Asset.list(job.getOutputAssetsLink()));

            AssetInfo intermediateSmoothAsset = outputAssets.get(0);
            // TODO: If you want to keep the smooth asset too, comment out the following:
            service.delete(Asset.delete(intermediateSmoothAsset.getId()));

            preparedAsset = outputAssets.get(1);

            // ** END ENCODE iOS **
        }

        // ** DELIVER AND STREAM **

        // Create a Java console app
        // Add Maven dependency on microsoft-windowsazure-api, version 0.4.0 or newer
        //
        // TODO: Uncomment the following if you are not using the previous snippets
        // Configuration config = MediaConfiguration.configureWithOAuthAuthentication(
        //         "https://media.windows.net/API/",
        //         "https://wamsprodglobal001acs.accesscontrol.windows.net/v2/OAuth2-13",
        //         "<Account name from portal>",
        //         "<Key from portal>",
        //         "urn:WindowsAzureMediaServices");
        //
        // MediaContract service = MediaService.create(config);

        // TODO: Replace with a retrieved asset if you are not using the previous snippets
        AssetInfo streamingAsset = preparedAsset;

        double minutesForWhichStreamingUrlIsActive = 365 /* days */ * 12 /* hours */ * 60 /* minutes */;
        Date tenMinutesAgo = new Date(
                new Date().getTime() - 10 /* minutes */ * 60 /* seconds */ * 1000 /* milliseconds */);

        AccessPolicyInfo streaming = service.create(AccessPolicy.create(streamingAsset.getName(),
                minutesForWhichStreamingUrlIsActive,
                EnumSet.of(AccessPolicyPermission.READ, AccessPolicyPermission.LIST)));

        String streamingUrl = "";

        ListResult<AssetFileInfo> assetFiles = service.list(AssetFile.list(streamingAsset.getAssetFilesLink()));
        AssetFileInfo streamingAssetFile = null;
        for (AssetFileInfo file : assetFiles) {
            if (file.getName().toLowerCase().endsWith("m3u8-aapl.ism")) {
                streamingAssetFile = file;
                break;
            }
        }

        if (streamingAssetFile != null) {
            LocatorInfo locator = service.create(
                    Locator.create(streaming.getId(), streamingAsset.getId(), LocatorType.OnDemandOrigin));
            URI hlsUri = new URI(locator.getPath() + streamingAssetFile.getName() + "/manifest(format=m3u8-aapl)");
            streamingUrl = hlsUri.toString();
        }

        if (streamingUrl.isEmpty()) {
            streamingAssetFile = null;
            for (AssetFileInfo file : assetFiles) {
                if (file.getName().toLowerCase().endsWith(".ism")) {
                    streamingAssetFile = file;
                    break;
                }
            }
            if (streamingAssetFile != null) {

                LocatorInfo locator = service.create(
                        Locator.create(streaming.getId(), streamingAsset.getId(), LocatorType.OnDemandOrigin));
                URI smoothUri = new URI(locator.getPath() + streamingAssetFile.getName() + "/manifest");
                streamingUrl = smoothUri.toString();
            }
        }

        if (streamingUrl.isEmpty()) {
            streamingAssetFile = null;
            for (AssetFileInfo file : assetFiles) {
                if (file.getName().toLowerCase().endsWith(".mp4")) {
                    streamingAssetFile = file;
                    break;
                }
            }
            if (streamingAssetFile != null) {

                LocatorInfo locator = service.create(
                        Locator.create(streaming.getId(), streamingAsset.getId(), LocatorType.SAS));
                URI mp4Uri = new URI(locator.getPath());
                mp4Uri = new URI(mp4Uri.getScheme(), mp4Uri.getUserInfo(), mp4Uri.getHost(), mp4Uri.getPort(),
                        mp4Uri.getPath() + "/" + streamingAssetFile.getName(),
                        mp4Uri.getQuery(), mp4Uri.getFragment());
                streamingUrl = mp4Uri.toString();
            }
        }

        System.out.println("Streaming Url: " + streamingUrl);
    }
}
