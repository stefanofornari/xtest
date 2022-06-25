/*
 * xTest
 * Copyright (C) 2022 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */
package org.testfx.assertions.api;

import javafx.css.Styleable;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Window;
import org.testfx.service.query.NodeQuery;

/**
 *
 * @author ste
 */
public class Then extends org.testfx.assertions.api.Assertions {

    protected Then() {}

    /**
     * Create assertion for {@link Button}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static ButtonAssert then(Button actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link Color}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static ColorAssert then(Color actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link ComboBox}.
     *
     * @param actual the actual value
     * @param <T> the type of the value contained in the {@link ComboBox}
     * @return the created assertion object
     */
    public static <T> ComboBoxAssert<T> then(ComboBox<T> actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link Dimension2D}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static Dimension2DAssert then(Dimension2D actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link Labeled}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static LabeledAssert then(Labeled actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link ListView}.
     *
     * @param actual the actual value
     * @param <T> the type of the value contained in the {@link ListView}
     * @return the created assertion object
     */
    public static <T> ListViewAssert<T> then(ListView<T> actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link MenuItem}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static MenuItemAssert then(MenuItem actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link Node}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static NodeAssert then(Node actual) {
        return new NodeAssert(actual);
    }

    /**
     * Create assertion for {@link Parent}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static ParentAssert then(Parent actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link Styleable}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static StyleableAssert then(Styleable actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link TableView}.
     *
     * @param actual the actual value
     * @param <T> the type of the value contained in the {@link TableView}
     * @return the created assertion object
     */
    public static <T> TableViewAssert<T> then(TableView<T> actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link Text}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static TextAssert then(Text actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link TextFlow}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static TextFlowAssert then(TextFlow actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link TextInputControl}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static TextInputControlAssert then(TextInputControl actual) {
        return assertThat(actual);
    }

    /**
     * Create assertion for {@link Window}.
     *
     * @param actual the actual value
     * @return the created assertion object
     */
    public static WindowAssert then(Window actual) {
        return assertThat(actual);
    }

    /**
     * Create an assertion for {@link NodeQuery}
     * @param actual the current value
     * @return the created assertion object
     */
    public static NodeQueryAssert then(NodeQuery actual) {
        return new NodeQueryAssert(actual);
    }

}
