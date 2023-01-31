package ru.eurekabpo.validatorxmlutil;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import ru.eurekabpo.validatorxmlutil.model.Element;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MapStaffObjectHandlerSax extends DefaultHandler {
  public static String PATH_TO_PARENT_FILE;
  private final List<String> algorithms = List.of("CBC", "MD2", "MD4", "MD5", "MAC", "SHA-1", "SHA-2", "SSL-3");
  private final StringBuilder currentValue = new StringBuilder();
  List<Element> result;
  Element currentElement;

  public List<Element> getResult() {
    return result;
  }

  @Override
  public void startDocument() {
    result = new ArrayList<>();
  }


  @Override
  public void startElement(
    String uri,
    String localName,
    String qName,
    Attributes attributes) {

    currentValue.setLength(0);

    if (qName.equalsIgnoreCase("hccdo:RegistrationDossierDocDetails")) {
      currentElement = new Element();
    }
  }

  @Override
  public void endElement(String uri,
                         String localName,
                         String qName) {
    if (qName.equalsIgnoreCase("hcsdo:DrugAttributeEnumText")) {
      if (currentValue.toString().length() > 30 && currentValue.toString().length()
        < 35 && !currentValue.toString().endsWith(".pdf")) {
        currentElement.setCheckSum(currentValue.toString());
      }
      if (algorithms.contains(currentValue.toString())) {
        currentElement.setAlgorithm(currentValue.toString());
      }
      if (currentValue.toString().endsWith(".pdf")) {
        String decodeBase64 = encodeBase64(Base64.encodeBase64String(
          readPDF(PATH_TO_PARENT_FILE + "/" + currentValue)), currentElement.getAlgorithm());
        currentElement.setBase64(decodeBase64);
      }
    }
    if (qName.equalsIgnoreCase("hcsdo:DocCopyBinaryText")) {
      currentElement.setBase64(encodeBase64(currentValue.toString(), currentElement.getAlgorithm()));
    }
    if (qName.equalsIgnoreCase("csdo:DocName")) {
      currentElement.setFileName(currentValue.toString());
    }
    if (qName.equalsIgnoreCase("hccdo:RegistrationDossierDocDetails")) {
      result.add(currentElement);
    }
  }

  public void characters(char[] ch, int start, int length) {
    currentValue.append(ch, start, length);

  }

  /**
   * Преобразует base64 по указанному алгоритму
   *
   * @param rawString Строка для расшифровки base64
   * @param algorithm Алгоритм шифрования
   * @return зашифрованная строка
   */
  private static String encodeBase64(String rawString, String algorithm) {
    byte[] bytes = Base64.decodeBase64(rawString);
    MessageDigest md;
    try {
      md = MessageDigest.getInstance(algorithm);
      md.update(bytes);
      return new BigInteger(1, md.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Читает PDF
   *
   * @param pathPDF Путь до PDF файла
   * @return массив байтов прочитанного файла.
   */
  private static byte[] readPDF(String pathPDF) {
    try (InputStream stream = new FileInputStream(pathPDF)) {
      byte[] buffer = new byte[8192];
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      int bytesRead;
      while ((bytesRead = stream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      return outputStream.toByteArray();
    } catch (IOException e) {
      System.out.println("ERROR: Не удалось прочитать PDF.");
      throw new RuntimeException(e);
    }
  }

}