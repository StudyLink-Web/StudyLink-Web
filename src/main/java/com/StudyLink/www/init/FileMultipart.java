package com.StudyLink.www.init;

import org.springframework.web.multipart.MultipartFile;
import java.io.*;

public class FileMultipart implements MultipartFile {
    private final byte[] fileContent;
    private final String originalFilename;
    private final String contentType;

    // ✅ InputStream + 파일명 생성자
    public FileMultipart(String originalFilename, InputStream is, String contentType) throws IOException {
        this.originalFilename = originalFilename;
        this.fileContent = is.readAllBytes();
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return originalFilename;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(fileContent);
        }
    }
}
