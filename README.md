В браузере использовать ссылки ниже:

http://localhost:9999/messages?reg=10&qwerty=7&name=Victor&lastName=Tkachenko
или
http://localhost:9999/?title=asdasd&value=asdag&value=qwe&image=book.txt
или через Insomnia
через Get будет выводиться тело запроса
через Post не будет выводиться тело запроса

Можно использовать html, расположенным по пути:
src/main/resources/default-get.html

Честно и прямо напишу - не знаю что делать с кириллицей.
Неправильно отображается и такое чувство, что длина ответа обрезается. Пока не понял, что с этим делать

Постарался придерживаться принципов SOLID, DRY и KISS (метод getQueryParam не в счет, по заданию нужно было реализовать)