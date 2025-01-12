/*
 * xTest
 * Copyright (C) 2025 Stefano Fornari
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
package ste.xtest.web;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 */
public class WebFiddle extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("WebFiddle v1.0");
        stage.setWidth(575);
        stage.setHeight(800);
        stage.setMaximized(true);
        //stage.getIcons().add(new Image(WebFiddle.class.getResourceAsStream("/images/easy-wallet-icon-64x64.png")));

        final WebView webView = new WebView();
        webView.getEngine().load("https://jsfiddle.net/");
        Scene scene = new Scene(webView);

        stage.setScene(scene);

        stage.show();
    }

}
