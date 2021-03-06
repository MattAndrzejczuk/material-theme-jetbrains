package com.chrisrm.idea;

import com.google.common.collect.ImmutableList;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.*;

import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MTTheme {
    DARKER("mt.darker", "Material Theme - Darker", true),
    DEFAULT("mt.default", "Material Theme - Default", true),
    LIGHTER("mt.lighter", "Material Theme - Lighter", false);

    @NonNls
    private static final String[] ourPatchableFontResources = {"Button.font", "ToggleButton.font", "RadioButton.font",
            "CheckBox.font", "ColorChooser.font", "ComboBox.font", "Label.font", "List.font", "MenuBar.font", "MenuItem.font",
            "MenuItem.acceleratorFont", "RadioButtonMenuItem.font", "CheckBoxMenuItem.font", "Menu.font", "PopupMenu.font", "OptionPane.font",
            "Panel.font", "ProgressBar.font", "ScrollPane.font", "Viewport.font", "TabbedPane.font", "Table.font", "TableHeader.font",
            "TextField.font", "FormattedTextField.font", "Spinner.font", "PasswordField.font", "TextArea.font", "TextPane.font", "EditorPane.font",
            "TitledBorder.font", "ToolBar.font", "ToolTip.font", "Tree.font"};


    private static final List<String> EDITOR_COLORS_SCHEMES;
    static {
        List<String> schemes = new ArrayList<String>();
        for (MTTheme theme : values()) {
            schemes.add(theme.editorColorsScheme);
        }
        EDITOR_COLORS_SCHEMES = ImmutableList.copyOf(schemes);
    }

    private final String id;
    private final String editorColorsScheme;
    private final boolean dark;

    MTTheme(@NotNull String id, @NotNull String editorColorsScheme, boolean dark) {
        this.id = id;
        this.editorColorsScheme = editorColorsScheme;
        this.dark = dark;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void activate() {
        try {
            UIManager.setLookAndFeel(new MTLaf(this));
            JBColor.setDark(dark);
            IconLoader.setUseDarkIcons(dark);

            PropertiesComponent.getInstance().setValue(getSettingsPrefix() + ".theme", name());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        String currentScheme = EditorColorsManager.getInstance().getGlobalScheme().getName();

        String makeActiveScheme = !EDITOR_COLORS_SCHEMES.contains(currentScheme) ?
                currentScheme : editorColorsScheme;

        EditorColorsScheme scheme = EditorColorsManager.getInstance().getScheme(makeActiveScheme);
        if (scheme != null) {
            EditorColorsManager.getInstance().setGlobalScheme(scheme);
        }

        UISettings uiSettings = UISettings.getInstance();
        uiSettings.fireUISettingsChanged();
        ActionToolbarImpl.updateAllToolbarsImmediately();

        UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();

        if (uiSettings.getOverrideLafFonts()) {
            JBUI.setUserScaleFactor(uiSettings.getFontSize() / 12f);
            initFontDefaults(uiDefaults, uiSettings.getFontFace(), uiSettings.getFontSize());
        }
    }

    static void initFontDefaults(UIDefaults defaults, String fontFace, int fontSize) {
        defaults.put("Tree.ancestorInputMap", null);
        FontUIResource uiFont = new FontUIResource(fontFace, Font.PLAIN, fontSize);
        FontUIResource textFont = new FontUIResource("Serif", Font.PLAIN, fontSize);
        FontUIResource monoFont = new FontUIResource("Monospaced", Font.PLAIN, fontSize);

        for (String fontResource : ourPatchableFontResources) {
            defaults.put(fontResource, uiFont);
        }

        defaults.put("PasswordField.font", monoFont);
        defaults.put("TextArea.font", monoFont);
        defaults.put("TextPane.font", textFont);
        defaults.put("EditorPane.font", textFont);
    }

    @Nullable
    public static MTTheme valueOfIgnoreCase(@Nullable String name) {
        for (MTTheme theme : MTTheme.values()) {
            if (theme.name().equalsIgnoreCase(name)) {
                return theme;
            }
        }
        return null;
    }

    @NotNull
    public static MTTheme getCurrentPreference() {
        String name = PropertiesComponent.getInstance().getValue(getSettingsPrefix() + ".theme");
        MTTheme theme = MTTheme.valueOfIgnoreCase(name);
        return theme == null ? MTTheme.DEFAULT : theme;
    }

    /**
     * @deprecated if more settings are needed for this plugin, you should create a {@link
     * com.intellij.openapi.components.PersistentStateComponent} and store all the settings in a separate file without
     * the prefix on the property name.
     */
    @Deprecated
    private static String getSettingsPrefix() {
        PluginId pluginId = PluginManager.getPluginByClassName(MTTheme.class.getName());
        return pluginId == null ? "com.chrisrm.idea.MaterialThemeUI" : pluginId.getIdString();
    }
}
