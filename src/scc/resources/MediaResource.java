package scc.resources;

import scc.srv.MainApplication;
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
import jakarta.ws.rs.core.Response;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource {

	// Get connection string in the storage access keys page
	String storageConnectionString = MainApplication.STORAGE_CONNECTION_STRING;

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
	public Response upload(byte[] contents) throws JsonProcessingException {

		String key = Hash.of(contents);
		BlobClient blob = getContainerClient().getBlobClient(key);
		blob.upload(BinaryData.fromBytes(contents), true);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(key);
		return Response.ok(json,MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if
	 * id does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(@PathParam("id") String photoId, String ownerId) {
		
		BlobClient blob = getContainerClient().getBlobClient(photoId);
		if (!blob.exists())
			throw new NotFoundException();
		BinaryData data = blob.downloadContent();
		return Response.ok(data.toBytes(),MediaType.APPLICATION_OCTET_STREAM).build();
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
