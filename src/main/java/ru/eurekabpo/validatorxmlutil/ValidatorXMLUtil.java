package ru.eurekabpo.validatorxmlutil;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.xml.sax.SAXException;
import ru.eurekabpo.validatorxmlutil.model.Element;
import ru.eurekabpo.validatorxmlutil.model.ReportData;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ValidatorXMLUtil {
  private static String PATH = Main.class.getProtectionDomain().getCodeSource()
    .getLocation().getPath().replace("GUIValidatorXML.jar", "");
  private static final Logger logger = Main.getLogger();

  /**
   * Подготовка пространства в папке для валидации доьсе.
   */
  public static void spacePreparation() {
    try {
      if (System.getProperty("os.name").startsWith("Windows")){
        PATH = PATH.substring(1);
      }
      System.out.println("Space preparation -"  + PATH);
      File xsd = new File(PATH + "/xsd");
      if (!xsd.exists()) {
        xsd.mkdir();
        logInfo("INFO", "Directory <XSD> is created");
        FileInputStream inputStream = new FileInputStream(PATH + "/GUIValidatorXML.jar");
        ZipInputStream zip = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
          if (entry.getName().endsWith("xsd")) {
            FileOutputStream fout = new FileOutputStream(PATH + "/" + entry.getName());
            for (int c = zip.read(); c != -1; c = zip.read()) {
              fout.write(c);
            }
            logInfo("INFO", "File <" + entry.getName() + "> is created");
            fout.flush();
            zip.closeEntry();
            fout.close();
          }
        }
      }
    } catch (IOException e) {
      logError("ERROR", "Unable to prepare space to run the program.");
      System.exit(1);
    }
  }

  /**
   * Проверяем предоставленный XML на соответствие предоставленным файлам схемы XSD.
   *
   * @param xmlFilePathAndName Путь до XML файла;
   * @return реультат проверки true/false
   */
  public static boolean validateXmlAgainstXsds(String xmlFilePathAndName) {
    List<String> xsdFilesPaths = new ArrayList<>();
    try (Stream<Path> filePathStream = Files.walk(Paths.get(PATH + "/xsd"))) {
      filePathStream.forEach(filePath -> {
        if (Files.isRegularFile(filePath)) {
          xsdFilesPaths.add(filePath.toString());
        }
      });
    } catch (IOException e) {
      System.exit(1);
    }

    if (xmlFilePathAndName == null || xmlFilePathAndName.isEmpty()) {
      logError("ERROR", "The path of the XML file being checked cannot be null.");
      System.exit(1);
    }
    if (xsdFilesPaths.size() < 1) {
      logError("ERROR", "There must be at least one XSD file.");
      System.exit(1);
    }
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    StreamSource[] xsdSources = generateStreamSourcesFromXsdPathsJdk8(xsdFilesPaths);
    try {
      Schema schema = schemaFactory.newSchema(xsdSources);
      Validator validator = schema.newValidator();
      logInfo("INFO", "Validation " + xmlFilePathAndName + " with XSDs " + xsdFilesPaths + "...");
      validator.validate(new StreamSource(new File(xmlFilePathAndName)));
    } catch (IOException | SAXException exception) {
      logError("ERROR", "ERROR: Failed to check " + xmlFilePathAndName + " with XSDs " + xsdFilesPaths + " - " + exception);
      System.exit(1);
    }
    logInfo("INFO", "Schema validation completed.");
    return true;
  }

  /**
   * Проверяем соответствие хэш сумм
   *
   * @param file XML файл
   */
  public static void equalsHashSum(File file) {
    SAXParserFactory factory = SAXParserFactory.newInstance();

    try (InputStream is = new FileInputStream(file)) {

      SAXParser saxParser = factory.newSAXParser();
      MapStaffObjectHandlerSax handler = new MapStaffObjectHandlerSax();
      MapStaffObjectHandlerSax.PATH_TO_PARENT_FILE = file.getParent();
      saxParser.parse(is, handler);
      List<Element> result = handler.getResult();
      List<ReportData> reportData = new ArrayList<>();
      boolean isValidFile, isValidDossier = true;
      for (int i = 0; i < result.size(); i++) {
        String checkSumStr = result.get(i).getCheckSum();
        String decodeBase64 = result.get(i).getBase64();
        checkSumStr = checkSumStr.replaceFirst("^0+(?!$)", "");
        isValidFile = decodeBase64.equalsIgnoreCase(checkSumStr);
        if (!isValidFile) {
          isValidDossier = false;
        }
        reportData.add(new ReportData(i + 1, result.get(i).getFileName(), checkSumStr, decodeBase64, isValidFile ? "OK" : "ERROR"));
      }
      saveReport(reportData);
      if (!isValidDossier) {
        logError("ERROR", "ERROR: Hash sums do not match");
        System.exit(1);
      } else {
        logInfo("INFO", "Hash sums check is complete.");
      }

    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Создает массив экземпляров StreamSource, представляющих XSD.
   *
   * @param xsdFilesPaths Список путей до файлов XSD.
   * @return Экземпляры StreamSource, представляющие XSD.
   */
  private static StreamSource[] generateStreamSourcesFromXsdPathsJdk8(List<String> xsdFilesPaths) {
    return xsdFilesPaths.stream()
      .map(StreamSource::new)
      .collect(Collectors.toList())
      .toArray(new StreamSource[xsdFilesPaths.size()]);
  }

  /**
   * Сохраняет отчёт
   *
   * @param reportData Список моделей данных для сохранения
   */
  private static void saveReport(List<ReportData> reportData) {
    String filePath;
    File nativeDir = new File(System.getProperty("java.io.tmpdir", "tmplib"));
    nativeDir.mkdirs();
    if (!nativeDir.isDirectory()) {
      logError("ERROR", "Unable to create native library working directory " + nativeDir);
      System.exit(1);
    }

    File trialJniSubDir = new File(nativeDir + File.separator + "validate-reports");
    trialJniSubDir.mkdir();

    SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
    filePath = trialJniSubDir.getAbsolutePath() + File.separator + "report-" + formatForDateNow.format(new Date()) + ".xls";

    try (FileOutputStream out = new FileOutputStream(filePath);
         HSSFWorkbook report = new HSSFWorkbook()) {
      HSSFSheet sheet = report.createSheet("Validator dossier");

      int rowNum = 0;

      Row row = sheet.createRow(rowNum);
      row.createCell(0).setCellValue("№");
      row.createCell(1).setCellValue("Имя файла");
      row.createCell(2).setCellValue("Хэш сумма");
      row.createCell(3).setCellValue("Вычисленная хэш сумма");
      row.createCell(4).setCellValue("Результат");

      for (ReportData data : reportData) {
        createSheetHeader(sheet, ++rowNum, data);
      }
      report.write(out);

    } catch (IOException e) {
      logError("ERROR", "Report has not been created.");
      System.exit(1);
    }
    logInfo("REPORT", filePath);
    logInfo("INFO", "Report has been created.");
  }

  private static void createSheetHeader(HSSFSheet sheet, int rowNum, ReportData reportData) {
    Row row = sheet.createRow(rowNum);
    row.createCell(0).setCellValue(reportData.getId());
    row.createCell(1).setCellValue(reportData.getFileName());
    row.createCell(2).setCellValue(reportData.getHashSum());
    row.createCell(3).setCellValue(reportData.getComputedHashSum());
    row.createCell(4).setCellValue(reportData.getResultValidation());
  }

  private static void println(PrintStream out, String msg) {
    out.println(msg);
    out.flush();
    logger.log(Level.INFO, msg);
  }

  private static void logError(String type, String msg) {
    println(System.err, String.format("[%s] %s", type, msg));
  }

  private static void logInfo(String type, String msg) {
    println(System.out, String.format("[%s] %s", type, msg));
  }
}
