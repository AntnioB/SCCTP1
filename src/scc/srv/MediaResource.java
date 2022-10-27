package scc.srv;

import scc.utils.Hash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource {

	// Get connection string in the storage access keys page
	String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=sccstwesteurope58152;AccountKey=dTLHRViEyorNqXTsjDkopsU6fw1TfIXZTBcqeJjFI1gtnmpDEjF4P+5AxamW453yVodXMWUUdTzn+ASt90iXTw==;EndpointSuffix=core.windows.net";

	private BlobContainerClient getContainerClient() {

		// Get container client
		BlobContainerClient containerClient = new BlobContainerClientBuilder()
				.connectionString(storageConnectionString)
				.containerName("images")
				.buildClient();

		return containerClient;
	}

	/**
	 * Post a new image.The id of the image is its hash.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		String key = Hash.of(contents);
		BlobClient blob = getContainerClient().getBlobClient(key);
		blob.upload(BinaryData.fromBytes(contents), true);
		return key;
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if
	 * id does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		BlobClient blob = getContainerClient().getBlobClient(id);
		if (!blob.exists())
			throw new NotFoundException();
		BinaryData data = blob.downloadContent();
		return data.toBytes();
	}

	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> list() {

		Iterator<BlobItem> it = getContainerClient().listBlobs().iterator();
		List<String> res = new ArrayList<>();

		while (it.hasNext()) {
			res.add(it.next().getName());
		}

		if (res.isEmpty())
			throw new NotFoundException();

		return res;
	}
}
