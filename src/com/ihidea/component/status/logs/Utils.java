/**
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package com.ihidea.component.status.logs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Misc. static helper methods.
 */
public final class Utils {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(Utils.class);

  /**
   * Prevent Instantiation.
   */
  private Utils() {
    // Prevent Instantiation
  }

  /**
   * Calc pool usage score.
   *
   * @param max the max
   * @param value the value
   * @return the int
   */
  public static int calcPoolUsageScore(int max, int value) {
    return max > 0 ? Math.max(0, value) * 100 / max : 0;
  }

  /**
   * Delete.
   *
   * @param file the file
   */
  public static void delete(File file) {
    if (file != null && file.exists()) {
      if (file.isDirectory()) {
        for (File child : file.listFiles()) {
          delete(child);
        }
      }
      if (!file.delete()) {
        logger.debug("Cannot delete '{}'", file.getAbsolutePath());
      }
    } else {
      logger.debug("'{}' does not exist", file);
    }
  }

  /**
   * Send file.
   *
   * @param request the request
   * @param response the response
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void sendFile(HttpServletRequest request, HttpServletResponse response, File file)
      throws IOException {

    try (OutputStream out = response.getOutputStream();
        RandomAccessFile raf = new RandomAccessFile(file, "r")) {
      long fileSize = raf.length();
      long rangeStart = 0;
      long rangeFinish = fileSize - 1;

      // accept attempts to resume download (if any)
      String range = request.getHeader("Range");
      if (range != null && range.startsWith("bytes=")) {
        String pureRange = range.replaceAll("bytes=", "");
        int rangeSep = pureRange.indexOf('-');

        try {
          rangeStart = Long.parseLong(pureRange.substring(0, rangeSep));
          if (rangeStart > fileSize || rangeStart < 0) {
            rangeStart = 0;
          }
        } catch (NumberFormatException e) {
          // keep rangeStart unchanged
          logger.trace("", e);
        }

        if (rangeSep < pureRange.length() - 1) {
          try {
            rangeFinish = Long.parseLong(pureRange.substring(rangeSep + 1));
            if (rangeFinish < 0 || rangeFinish >= fileSize) {
              rangeFinish = fileSize - 1;
            }
          } catch (NumberFormatException e) {
            logger.trace("", e);
          }
        }
      }

      // set some headers
      response.setContentType("application/x-download");
      response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
      response.setHeader("Accept-Ranges", "bytes");
      response.setHeader("Content-Length", Long.toString(rangeFinish - rangeStart + 1));
      response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeFinish + "/"
          + fileSize);

      // seek to the requested offset
      raf.seek(rangeStart);

      // send the file
      byte[] buffer = new byte[4096];

      long len;
      int totalRead = 0;
      boolean nomore = false;
      while (true) {
        len = raf.read(buffer);
        if (len > 0 && totalRead + len > rangeFinish - rangeStart + 1) {
          // read more then required?
          // adjust the length
          len = rangeFinish - rangeStart + 1 - totalRead;
          nomore = true;
        }

        if (len > 0) {
          out.write(buffer, 0, (int) len);
          totalRead += len;
          if (nomore) {
            break;
          }
        } else {
          break;
        }
      }
    }
  }

  /**
   * Send compressed file.
   *
   * @param request the request
   * @param response the response
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void sendCompressedFile(HttpServletRequest request, HttpServletResponse response, File file)
      throws IOException {
    try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream());
        InputStream fileInput = new BufferedInputStream(new FileInputStream(file))) {
      // set some headers
      response.setContentType("application/zip");
      response.setHeader("Content-Disposition", "attachment; filename=" + file.getName() + ".zip");

      zip.putNextEntry(new ZipEntry(file.getName()));

      // send the file
      byte[] buffer = new byte[4096];
      long len;

      while ((len = fileInput.read(buffer)) > 0) {
        zip.write(buffer, 0, (int) len);
      }
      zip.closeEntry();
    }
  }
}
