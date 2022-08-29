<big><b>ru.parser.hyundai</b></big> версия 1.01<br> 
 
Парсинг автомобилей у дилеров hyundai<br><br> 

<b>требования:</b><br>&nbsp;&nbsp;&nbsp;&nbsp; java 11<br>
<b>зависимости:</b><br>&nbsp;&nbsp;&nbsp;&nbsp; json-simple-1.1.1.jar<br>&nbsp;&nbsp;&nbsp;&nbsp; jsoup-1.15.5.jar

<b>для запуска необходимо указать:</b><br>
<b>String city</b> - город, для поиска<br>
<b>String modelName</b> - наименование модели<br>
<b>String carConfig</b> - комплектация<br>
<b>String filePath</b> - путь к html-файлу со списком дилеров (страница с выбором дилера)<br>

<b>Версия 1.01, отличия от прошлой версии:</b><br>
* параметры можно указать в командной строке, если они не указаны, берутся значения по-умолчанию из кода
* в отчет добавлен цвет кузова авто

