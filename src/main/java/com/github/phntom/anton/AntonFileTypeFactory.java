package com.github.phntom.anton;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class AntonFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(AntonFileType.INSTANCE, AntonFileType.EXACT_FILENAME_MATCHER, AntonFileType.EXTENSION_MATCHER);
    }
}
