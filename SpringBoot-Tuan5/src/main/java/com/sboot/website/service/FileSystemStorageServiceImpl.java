package com.sboot.website.service;

public class FileSystemStorageServiceImpl implements StorageService {
	private final Path rootLocation;
	@Override
	public String getSorageFilename(MultipartFile file, String id) {
	String ext = FilenameUtils.getExtension(file.getOriginalFilename());
	return "p" + id + "." + ext;
	}
	public FileSystemStorageServiceImpl(StorageProperties properties) {
	this.rootLocation = Paths.get(properties.getLocation());
	}
	@Override
	public void store(MultipartFile file, String storeFilename) {
	try {
	if(file.isEmpty()) {
	throw new StorageException("Failed to store empty file");
	}
	Path destinationFile =
	this.rootLocation.resolve(Paths.get(storeFilename))
	.normalize().toAbsolutePath(); //lấy đường dẫn tuyệt
	đối
	if(!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
	throw new StorageException("Cannot store file outside
	curent directory");
	}
	try (InputStream inputStream = file.getInputStream()) {
	Files.copy(inputStream, destinationFile,
	StandardCopyOption.REPLACE_EXISTING);
	}
	} catch (Exception e) {
	throw new StorageException("Failed to store file: ", e);
	}
	}
	@Override
	public Resource loadAsResource(String filename) {
	try {
	Path file = load(filename);
	Resource resource = new UrlResource(file.toUri());
	if(resource.exists() || resource.isReadable()) {
	return resource;
	}
	throw new StorageException("Can not read file: " + filename);
	} catch (Exception e) {
	throw new StorageException("Could not read file: " + filename);
	}
	}
	@Override
	public Path load(String filename) {
	return rootLocation.resolve(filename);
	}
	@Override
	public void delete(String storeFilename) throws Exception {
	Path destinationFile =
	rootLocation.resolve(Paths.get(storeFilename)).normalize().toAbsolutePath();
	Files.delete(destinationFile);
	}
	@Override
	public void init() {
	try {
	Files.createDirectories(rootLocation);
	System.out.println(rootLocation.toString());
	} catch (Exception e) {
	throw new StorageException("Could not read file: ", e);
	}
	}
	}
}
