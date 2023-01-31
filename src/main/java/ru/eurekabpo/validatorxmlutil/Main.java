package ru.eurekabpo.validatorxmlutil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
  private static Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) throws Exception {
    String fileName = null;
    String logFile = null;

    for (String str : args) {
      if (str.startsWith("-file=")) {
        fileName = str.replaceFirst("-file=", "");
      }
      if (str.startsWith("-log=")) {
        logFile = str.replaceFirst("-log=", "");
      }
    }

    if (logFile != null) {
      FileHandler fh = new FileHandler(logFile, true);
      logger.addHandler(fh);
      SimpleFormatter formatter = new SimpleFormatter();
      fh.setFormatter(formatter);
    } else {

      logFile = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()
        .replace("GUIValidatorXML.jar", "")  + "logs";

      if (System.getProperty("os.name").startsWith("Windows")){
        logFile = logFile.substring(1);
      }
      System.out.println("Log file - " + logFile);
      File file = new File(logFile);
      if (!file.exists()) {
        file.mkdir();
      }
      SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
      logFile = logFile + File.separator + "log-" + formatForDateNow.format(new Date());
      FileHandler fh = new FileHandler(logFile, true);
      logger.addHandler(fh);
      SimpleFormatter formatter = new SimpleFormatter();
      fh.setFormatter(formatter);
    }

    if (fileName != null) {
      ValidatorXMLUtil.spacePreparation();
      ValidatorXMLUtil.validateXmlAgainstXsds(fileName);
      ValidatorXMLUtil.equalsHashSum(new File(fileName));
    } else {
      System.out.println("Params:\n-file={path to dossier}\n-log={path to file log}\n");
      System.exit(1);
    }
  }

  public static Logger getLogger() {
    return logger;
  }
}
