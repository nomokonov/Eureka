не используемы конструктор
public Element(String checkSum, String fileName, String algorithm, String base64, String pdfRef) {
ru/eurekabpo/validatorxmlutil/model/Element.java:14
public ReportData() {
ru/eurekabpo/validatorxmlutil/model/ReportData.java:10

Модификатор доступа
 List<Element> result;    + private
 Element currentElement;   + private
ru/eurekabpo/validatorxmlutil/MapStaffObjectHandlerSax.java:22

public String[] getPathToUtilExec() {   - private
ru/eurekabpo/validatorxmlgui/MainWindow.java:186

public void initReportButton() { - private
ru/eurekabpo/validatorxmlgui/MainWindow.java:69

убрать this. - не нужно  
this.frame.add(progressBar);
ru/eurekabpo/validatorxmlgui/MainWindow.java:66
ru/eurekabpo/validatorxmlgui/MainWindow.java:94
ru/eurekabpo/validatorxmlgui/MainWindow.java:98
ru/eurekabpo/validatorxmlgui/MainWindow.java:117у

Заменить на null-safe сравнение
qName.equalsIgnoreCase("hcsdo:DrugAttributeEnumText") -.
"hcsdo:DrugAttributeEnumText".equalsIgnoreCase(qName)
ru/eurekabpo/validatorxmlutil/MapStaffObjectHandlerSax.java:53
ru/eurekabpo/validatorxmlutil/MapStaffObjectHandlerSax.java:67
ru/eurekabpo/validatorxmlutil/MapStaffObjectHandlerSax.java:73

Добавить аннотацию   @Override  - удобнее читать код
public void characters(char[] ch, int start, int length) {
ru/eurekabpo/validatorxmlutil/MapStaffObjectHandlerSax.java:78


throw new RuntimeException(e);  - необрабатываемое исключение , заменить на свои, отражающие суть возникшей проблемы.
.waitFor() требует обработки InterruptedException.

.stop()  не рекомендуется к использованию, заменяем на .interrupt()  с обработкой флага в потоке
ru/eurekabpo/validatorxmlgui/MainWindow.java:140


catch (IOException e) {
System.exit(1); нужно хотябы залогировать ошибку
}
ru/eurekabpo/validatorxmlutil/ValidatorXMLUtil.java:87

new ZipInputStream new FileInputStream()  нужно использовать try c ресурсами ( для ресурсов)   



