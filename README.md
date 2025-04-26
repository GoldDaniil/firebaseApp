добавить у каждого из центров свои активные цели и задачи
а так же возможность перенсти активную задачу завершить ее - каким образом? нажать кнопку сделано - при нажатии появляется поле типо опишите свое решение проблемы
и кнопку отправить - после нажатия отправить блок с задачей отправляется в блок сделанных задач + фильтрация обычная по выполнению

то есть сделать кнопку показать задачи - при нажатии появляется блок с активными задачами - и кнопка показать выполенненые задачи -




мессенджер:

на вкладке мессенджер видно все созданные чаты как в телеге или ватсапе, если чатов нет - то писать что пока чатов нет

в правом углу кнопка создать чат : открывается меню: выбираешь юзера и нажимаешь кнопку создать - открывается с ним чат, по юзеру видно его : почту, ник
в шапке чата видно инфу о юзере , его почту
отправка смс через кнопку отправить 




rules : которые фикстят создание чата

rules_version = '2';
service cloud.firestore {
match /databases/{database}/documents {
match /centers/{centerId}/tasks/{taskId} {
allow read, write: if request.auth != null;
}
match /users/{userId} {
allow read: if request.auth != null;
allow write: if request.auth != null && request.auth.uid == userId;
}
match /centers/{centerId} {
allow read: if request.auth != null;
}
match /chats/{chatId} {
allow read, write: if request.auth != null;
match /messages/{messageId} {
allow read, write: if request.auth != null;
}
}
}
}
