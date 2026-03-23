

javac -cp "lib/*" -d bin src/com/quiz/util/*.java src/com/quiz/model/*.java src/com/quiz/dao/*.java src/com/quiz/ui/*.java src/com/quiz/Main.java


java -cp "bin;lib/*" com.quiz.Main

The app now:
- uses hashed passwords instead of plain text
- supports Student Login and Admin Login on the same screen
- supports admin-approved password reset requests with temporary passwords




