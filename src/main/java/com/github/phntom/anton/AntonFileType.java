package com.github.phntom.anton;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AntonFileType extends LanguageFileType {
    public static final AntonFileType INSTANCE = new AntonFileType();
    public static final FileNameMatcher EXACT_FILENAME_MATCHER = new ExactFileNameMatcher("Anton", false);
    public static final FileNameMatcher EXTENSION_MATCHER = new ExtensionFileNameMatcher("anton");
    public static final ImageIcon ICON = new ImageIcon(AntonFileType.class.getResource("/icons/antonIconFileType.png"));

    protected AntonFileType() {
        super(JsonLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Anton";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Anton's Plugin Settings and Bootstrap";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "anton";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return ICON;
    }
}
