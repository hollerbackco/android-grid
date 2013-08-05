package com.moziy.hollerback.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

public class FileCompressionUtil {

	public static void compress(File input, File output) throws IOException {
		FileInputStream fis = new FileInputStream(input);
		FileOutputStream fos = new FileOutputStream(output);
		GZIPOutputStream gzipStream = new GZIPOutputStream(fos);
		IOUtils.copy(fis, gzipStream);
		gzipStream.close();
		fis.close();
		fos.close();
	}

	public static void decompress(File input, File output) throws IOException {
		FileInputStream fis = new FileInputStream(input);
		FileOutputStream fos = new FileOutputStream(output);
		GZIPInputStream gzipStream = new GZIPInputStream(fis);
		IOUtils.copy(gzipStream, fos);
		gzipStream.close();
		fis.close();
		fos.close();
	}
}
