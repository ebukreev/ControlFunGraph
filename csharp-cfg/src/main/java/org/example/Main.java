package org.example;

import java.io.*;

public class Main {
    public static void createCsharpCfg(String inputCsharpCode) {

        try {
            String os = System.getProperty("os.name").toLowerCase();

            // Определяем путь к исполняемому файлу программы на C# в зависимости от операционной системы
            String csharpProgramPath;
            if (os.contains("win")) {
                // Windows
                csharpProgramPath = "csharp-cfg\\src\\main\\java\\org\\example\\net6.0\\win-x64\\Hackathon.exe";
            } else if (os.contains("nix") || os.contains("nux")) {
                // TODO
                // Linux
                var filePath = "net6.0/linux-x64/Hackathon";
                var curFile = new File(filePath);

                // Создаем процесс для выполнения chmod +x
                Process chmodProcess = new ProcessBuilder("chmod", "+x", curFile.getAbsolutePath()).start();
                chmodProcess.waitFor();

                csharpProgramPath = curFile.getAbsolutePath();

            } else if (os.contains("mac")) {
                // MacOS
                var filePath = "net6.0/osx-x64/Hackathon";
                var curFile = new File(filePath);

                // Создаем процесс для выполнения chmod +x
                Process chmodProcess = new ProcessBuilder("chmod", "+x", curFile.getAbsolutePath()).start();
                chmodProcess.waitFor();

                csharpProgramPath = curFile.getAbsolutePath();
            } else {
                System.out.println("Unsupported operating system");
                return;
            }

            // Создаем процесс для выполнения программы на C#
            Process process = new ProcessBuilder(csharpProgramPath).start();

            // Получаем входной и выходной потоки для взаимодействия с терминалом
            OutputStream outputStream = process.getOutputStream();
            InputStream inputStream = process.getInputStream();

            outputStream.write(inputCsharpCode.getBytes());
            outputStream.flush();
            outputStream.close();

            // Читаем результат из терминала
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Ждем завершения процесса
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        // Передаем строку в программу на C#
        String inputString =
                "private static int TestContinue(int x, int y)\n" +
                        "{\n" +
                        "    int c = 10;\n" +
                        "    c++;\n" +
                        "    int a = 5;\n" +
                        "    String b = \"str\";\n" +
                        "    for (int k = 0; k < a; k++)\n" +
                        "    {\n" +
                        "        for (int i = 1; i < c; i++)\n" +
                        "        {\n" +
                        "            if (c != a)\n" +
                        "            {\n" +
                        "                continue;\n" +
                        "            }\n" +
                        "             else\n" +
                        "             {\n" +
                        "                   if (c == a)\n" +
                        "                   {\n" +
                        "                       break;\n" +
                        "                   }\n" +
                        "                c++;\n" +
                        "            }\n" +
                        "            c--;\n" +
                        "        }\n" +
                        "    }\n" +
                        "    int result = c;\n" +
                        "    return result;\n" +
                        "}\n" +
                        "!!!END!!!";

        // Тестируем функцию
        createCsharpCfg(inputString);
    }
}
