package io.github.defective4.audioanalyzer;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        try (TensorflowAnalyzer analyzer = new TensorflowAnalyzer("/opt/venv")){
            System.out.println(analyzer.analyze(new File("/tmp/Fish.mp3")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
