package org.springframework.ai.transformers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

public class ResourceCacheService {

	private static final Log logger = LogFactory.getLog(ResourceCacheService.class);

	private final File cacheDirectory;

	private List<String> excludedUriSchemas = new ArrayList<>(List.of("file", "classpath"));

	public ResourceCacheService() {
		this(new File(System.getProperty("java.io.tmpdir"), "spring-ai-onnx-generative").getAbsolutePath());
	}

	public ResourceCacheService(String rootCacheDirectory) {
		this(new File(rootCacheDirectory));
	}

	public ResourceCacheService(File rootCacheDirectory) {
		Assert.notNull(rootCacheDirectory, "Cache directory can not be null.");
		this.cacheDirectory = rootCacheDirectory;
		if (!this.cacheDirectory.exists()) {
			logger.info("Create cache root directory: " + this.cacheDirectory.getAbsolutePath());
			this.cacheDirectory.mkdirs();
		}
		Assert.isTrue(this.cacheDirectory.isDirectory(), "The cache folder must be a directory");
	}

	public void setExcludedUriSchemas(List<String> excludedUriSchemas) {
		Assert.notNull(excludedUriSchemas, "The excluded URI schemas list can not be null");
		this.excludedUriSchemas = excludedUriSchemas;
	}

	public Resource getCachedResource(String originalResourceUri) {
		return this.getCachedResource(new DefaultResourceLoader().getResource(originalResourceUri));
	}

	public Resource getCachedResource(Resource originalResource) {
		try {
			if (this.excludedUriSchemas.contains(originalResource.getURI().getScheme())) {
				logger.info("The " + originalResource.toString() + " resource with URI schema ["
						+ originalResource.getURI().getScheme() + "] is excluded from caching");
				return originalResource;
			}

			File cachedFile = getCachedFile(originalResource);
			if (!cachedFile.exists()) {
				FileCopyUtils.copy(StreamUtils.copyToByteArray(originalResource.getInputStream()), cachedFile);
				logger.info("Caching the " + originalResource.toString() + " resource to: " + cachedFile);
			}
			return new FileUrlResource(cachedFile.getAbsolutePath());
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to cache the resource: " + originalResource.getDescription(), e);
		}
	}

	private File getCachedFile(Resource originalResource) throws IOException {
		var resourceParentFolder = new File(this.cacheDirectory,
				UUID.nameUUIDFromBytes(pathWithoutLastSegment(originalResource.getURI())).toString());
		resourceParentFolder.mkdirs();
		String newFileName = getCacheName(originalResource);
		return new File(resourceParentFolder, newFileName);
	}

	private byte[] pathWithoutLastSegment(URI uri) {
		String path = uri.toASCIIString();
		var pathBeforeLastSegment = path.substring(0, path.lastIndexOf('/') + 1);
		return pathBeforeLastSegment.getBytes();
	}

	private String getCacheName(Resource originalResource) throws IOException {
		String fileName = originalResource.getFilename();
		String fragment = originalResource.getURI().getFragment();
		return !StringUtils.hasText(fragment) ? fileName : fileName + "_" + fragment;
	}

	public void deleteCacheFolder() {
		if (this.cacheDirectory.exists()) {
			logger.info("Empty Model Cache at:" + this.cacheDirectory.getAbsolutePath());
			this.cacheDirectory.delete();
			this.cacheDirectory.mkdirs();
		}
	}

}
