package org.example;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class Main {
    Path binaryPath;

    public Main() throws IOException, URISyntaxException, InterruptedException {
        initializeBinary();
    }

    private void initializeBinary() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        // Определяем путь к исполняемому файлу программы на C# в зависимости от операционной системы

        if (os.contains("win")) {
            // Windows
            binaryPath = Path.of(System.getenv("APPDATA")).resolve("csharp-cfg/Hackathon.exe");

        } else {
            // Linux or macos
            binaryPath = Path.of(System.getProperty("user.home")).resolve("csharp-cfg/Hackathon");

            // Создаем процесс для выполнения chmod +x
            Process chmodProcess = new ProcessBuilder("chmod", "+x", binaryPath.toAbsolutePath().toString()).start();
            chmodProcess.waitFor();
        }
    }

    public String buildCfg(String inputCsharpCode) {
        StringBuilder result = new StringBuilder();

        try {
            // Создаем процесс для выполнения программы на C#
            var pb = new ProcessBuilder(binaryPath.toAbsolutePath().toString());
            pb.directory(binaryPath.getParent().toFile());
            Process process = pb.start();

            // Получаем входной и выходной потоки для взаимодействия с терминалом
            OutputStream outputStream = process.getOutputStream();
            InputStream inputStream = process.getInputStream();
            InputStream errStream = process.getErrorStream();

            outputStream.write((inputCsharpCode + "\n!!!END!!!").getBytes());
            outputStream.flush();
            outputStream.close();

            // Читаем результат из терминала
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
                System.out.println(line);
            }

            reader = new BufferedReader(new InputStreamReader(errStream));
            while ((line = reader.readLine()) != null) {
                //result.append(line).append("\n");
                System.err.println(line);
            }

            // Ждем завершения процесса
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    public static void main(String[] args) {

        // Передаем строку в программу на C#
        String inputString =
                """
                        private static int Test2(int x, int y)
                        {
                            var c = 0;
                            c++;
                            int a = 5;
                            var b = "str";
                                                
                            for (var i = 0; i < a; i--)
                            {
                                c += a;
                                if (c < 1)
                                {
                                    c++;
                                    if (a < 12)
                                    {
                                        a += 1;
                                    }
                                }
                                                
                                a -= c;
                            }
                                                
                            var result = c;
                            return result;
                        }
                        """;

        // Тестируем функцию
        try {
            System.out.println(new Main().buildCfg(inputString));
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
