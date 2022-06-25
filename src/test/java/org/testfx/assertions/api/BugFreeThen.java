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

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

public class BugFreeThen extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        Label lbl1 = new Label("Text 1"); lbl1.setId("T1");
        Button btn1 = new Button("Button 1"); btn1.setId("B1");
        Button btn2 = new Button("Button 2"); btn2.setId("B2");
        StackPane pane = new StackPane(lbl1, btn1, btn2);

        stage.setScene(new Scene(pane, 200, 200));
        stage.show();
    }

    @Test
    public void some_queries() {
        Then.then(lookup(".text")).hasNWidgets(3);
        Then.then(lookup(".button .text")).hasNWidgets(2);
        Then.then(lookup("#T1 .text")).hasOneWidget();
        Then.then(lookup("#B2 .text")).hasOneWidget();
    }

    @Test
    public void find_exactly_one_widget() {
        Then.then(lookup(".label")).hasOneWidget();
        Then.then(lookup(".root")).hasOneWidget(); // StackPane
        Then.then(lookup("#B1")).hasOneWidget();

        try {
            Then.then(lookup(".button")).hasOneWidget();
            fail("no assertion error");
        } catch (AssertionError x) {
            then(x).hasMessageContaining(
                "Expected 1 result from query"
            ).hasMessageContaining(
                "lookup by selector: \".button\""
            ).hasMessageContaining(
                "but found 2 "
            ).hasMessageContaining(
                String.valueOf(lookup(".button").queryAll())
            );
        }

        try {
            Then.then(lookup(".radio")).hasOneWidget();
            fail("no assertion error");
        } catch (AssertionError x) {
            then(x).hasMessageContaining(
                "Expected 1 result from query"
            ).hasMessageContaining(
                "lookup by selector: \".radio\""
            ).hasMessageContaining(
                "but found nothing"
            );
        }
    }

    @Test
    public void find_n_widget() {
        Then.then(lookup(".text")).hasNWidgets(3);
        Then.then(lookup(".button")).hasNWidgets(2);

        try {
            Then.then(lookup(".button")).hasNWidgets(-1);
            fail("no argument sanity check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("n can not be negative");
        }

        try {
            Then.then(lookup(".button")).hasNWidgets(3);
            fail("no assertion error");
        } catch (AssertionError x) { // TODO <- which exception?
            then(x).hasMessageContaining(
                "Expected 3 results from query"
            ).hasMessageContaining(
                "lookup by selector: \".button\""
            ).hasMessageContaining(
                "but found 2 "
            ).hasMessageContaining(
                String.valueOf(lookup(".button").queryAll())
            );
        }

        try {
            Then.then(lookup(".button")).hasNWidgets(0);
            fail("no assertion error");
        } catch (AssertionError x) {
            then(x).hasMessageContaining(
                "Expected 0 results from query"
            ).hasMessageContaining(
                "lookup by selector: \".button\""
            ).hasMessageContaining(
                "but found 2 "
            ).hasMessageContaining(
                String.valueOf(lookup(".button").queryAll())
            );
        }

    }

    @Test
    public void find_at_least_one_widget() {
        Then.then(lookup(".button")).hasWidgets();


        try {
            Then.then(lookup(".radio")).hasWidgets();
            fail("no assertion error");
        } catch (AssertionError x) {
            then(x).hasMessageContaining(
                "Expected something from query"
            ).hasMessageContaining(
                "lookup by selector: \".radio\""
            ).hasMessageContaining(
                "but found nothing"
            );
        }
    }

    @Test
    public void find_no_widgets() {
        Then.then(lookup(".radio")).hasNoWidgets();

        try {
            Then.then(lookup(".label")).hasNoWidgets();
            fail("no assertion error");
        } catch (AssertionError x) {
            then(x).hasMessageContaining(
                "Expected nothing from query"
            ).hasMessageContaining(
                "lookup by selector: \".label\""
            ).hasMessageContaining(
                "but found 1 "
            ).hasMessageContaining(
                String.valueOf(lookup(".label").queryAll())
            );
        }

        try {
            Then.then(lookup(".text")).hasNoWidgets();
            fail("no assertion error");
        } catch (AssertionError x) {
            then(x).hasMessageContaining(
                "Expected nothing from query"
            ).hasMessageContaining(
                "lookup by selector: \".text\""
            ).hasMessageContaining(
                "but found 3 "
            ).hasMessageContaining(
                String.valueOf(lookup(".text").queryAll())
            );
        }
    }

}
