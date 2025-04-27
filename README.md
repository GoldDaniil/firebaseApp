

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


название заменить на Volunteer
