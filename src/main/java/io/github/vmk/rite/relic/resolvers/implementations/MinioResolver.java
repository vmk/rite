package io.github.vmk.rite.relic.resolvers.implementations;

import io.github.vmk.rite.relic.Relic;
import io.github.vmk.rite.relic.exceptions.RelicException;
import io.github.vmk.rite.relic.resolvers.RelicResolver;
import io.github.vmk.rite.relic.resolvers.ResolverDescriptor;
import io.minio.MinioClient;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MinioResolver implements RelicResolver {
    public static final String ENVIRONMENT = "minio";

    public enum DescriptorKeys {
        MINIOHOST("miniohost"),
        ACCESKEY("accesskey"),
        SECRETKEY("secretkey"),
        BUCKET("bucket");

        private final String key;

        private DescriptorKeys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    @Override
    public void resolveInternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        try {
            MinioClient minioClient = new MinioClient(rd.getProperty(DescriptorKeys.MINIOHOST.getKey()), rd.getProperty(DescriptorKeys.ACCESKEY.getKey()), rd.getProperty(DescriptorKeys.SECRETKEY.getKey()));
            File internalFile = new File(cacheDirectory, r.getFileName());
            String bucketName = rd.getProperty(DescriptorKeys.BUCKET.getKey());
            InputStream minioStream = minioClient.getObject(bucketName, r.getFileName());
            FileOutputStream fos = new FileOutputStream(internalFile);
            IOUtils.copyLarge(minioStream, fos);
            minioStream.close();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RelicException("Could not retrieve the file to the working directory", e);
        }
    }

    @Override
    public void resolveExternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        try {
            MinioClient minioClient = new MinioClient(rd.getProperty(DescriptorKeys.MINIOHOST.getKey()), rd.getProperty(DescriptorKeys.ACCESKEY.getKey()), rd.getProperty(DescriptorKeys.SECRETKEY.getKey()));
            String bucketName = rd.getProperty(DescriptorKeys.BUCKET.getKey());
            File internalFile = new File(cacheDirectory, r.getFileName());
            boolean isExist = minioClient.bucketExists(bucketName);
            if (isExist) {
                // NOP
            } else {
                minioClient.makeBucket(bucketName);
            }
            minioClient.putObject(bucketName, r.getFileName(), internalFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RelicException("Could not copy the file to the minio bucket", e);
        }
    }

    @Override
    public void deleteExternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        try {
            MinioClient minioClient = new MinioClient(rd.getProperty(DescriptorKeys.MINIOHOST.getKey()), rd.getProperty(DescriptorKeys.ACCESKEY.getKey()), rd.getProperty(DescriptorKeys.SECRETKEY.getKey()));
            String bucketName = rd.getProperty(DescriptorKeys.BUCKET.getKey());
            minioClient.removeObject(bucketName, r.getFileName());
        } catch (Exception e) {
            throw new RelicException("Could not delete the file in the minio bucket", e);
        }
    }

    @Override
    public void deleteInternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        File internalFile = new File(cacheDirectory, r.getFileName());
        internalFile.delete();
    }
}
