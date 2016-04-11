/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 28.12.15.
 */
public class DashboardSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 200;
    private static final double PREFERRED_HEIGHT = 148;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double ASPECT_RATIO     = 0.74;
    private static final double ANGLE_RANGE      = 180;

    private double        size;
    private double        width;
    private double        height;
    private double        centerX;
    private double        currentValueAngle;
    private Pane          pane;
    private Text          unitText;
    private Text          titleText;
    private Text          valueText;
    private Text          minText;
    private Text          maxText;
    private Path          barBackground;
    private MoveTo        barBackgroundStart;
    private ArcTo         barBackgroundOuterArc;
    private LineTo        barBackgroundLineToInnerArc;
    private ArcTo         barBackgroundInnerArc;
    private Path          dataBar;
    private MoveTo        dataBarStart;
    private ArcTo         dataBarOuterArc;
    private LineTo        dataBarLineToInnerArc;
    private ArcTo         dataBarInnerArc;
    private Line          threshold;
    private Text          thresholdText;
    private InnerShadow   innerShadow;
    private Font          smallFont;
    private Font          bigFont;
    private double        range;
    private double        angleStep;
    private boolean       colorGradientEnabled;
    private int           noOfGradientStops;
    private boolean       sectionsVisible;
    private List<Section> sections;
    private String        formatString;
    private String        otherFormatString;
    private Locale        locale;
    private double        minValue;


    // ******************** Constructors **************************************
    public DashboardSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        range                = gauge.getRange();
        angleStep            = ANGLE_RANGE / range;
        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();
        sectionsVisible      = gauge.getSectionsVisible();
        sections             = gauge.getSections();
        currentValueAngle    = 0;
        formatString         = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        otherFormatString    = new StringBuilder("%.").append(Integer.toString(gauge.getTickLabelDecimals())).append("f").toString();
        locale               = gauge.getLocale();
        
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        unitText = new Text(getSkinnable().getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setFill(getSkinnable().getUnitColor());

        boolean isUnitVisible = !getSkinnable().getUnit().isEmpty();
        unitText.setVisible(isUnitVisible);
        unitText.setManaged(isUnitVisible);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(getSkinnable().getTitleColor());

        boolean isTitleVisible = !getSkinnable().getTitle().isEmpty();
        unitText.setVisible(isTitleVisible);
        unitText.setManaged(isTitleVisible);

        valueText = new Text(String.format(locale, formatString, getSkinnable().getValue()));
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(getSkinnable().getValueColor());

        boolean isValueVisible = getSkinnable().isValueVisible();
        valueText.setVisible(isValueVisible);
        valueText.setManaged(isValueVisible);

        minValue = getSkinnable().getMinValue();
        minText  = new Text(String.format(locale, otherFormatString, minValue));
        minText.setTextOrigin(VPos.CENTER);
        minText.setFill(getSkinnable().getValueColor());

        maxText = new Text(String.format(locale, otherFormatString, getSkinnable().getMaxValue()));
        maxText.setTextOrigin(VPos.CENTER);
        maxText.setFill(getSkinnable().getValueColor());

        boolean tickLabelsVisible = getSkinnable().getTickLabelsVisible();
        minText.setVisible(tickLabelsVisible);
        minText.setManaged(tickLabelsVisible);
        maxText.setVisible(tickLabelsVisible);
        maxText.setManaged(tickLabelsVisible);

        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.3), 30d, 0d, 0d, 10d);

        barBackgroundStart          = new MoveTo(0, 0.675 * PREFERRED_HEIGHT);
        barBackgroundOuterArc       = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, true, true);
        barBackgroundLineToInnerArc = new LineTo(0.72222 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT);
        barBackgroundInnerArc       = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, false, false);

        barBackground = new Path();
        barBackground.setFillRule(FillRule.EVEN_ODD);
        barBackground.getElements().add(barBackgroundStart);
        barBackground.getElements().add(barBackgroundOuterArc);
        barBackground.getElements().add(barBackgroundLineToInnerArc);
        barBackground.getElements().add(barBackgroundInnerArc);
        barBackground.getElements().add(new ClosePath());
        barBackground.setFill(getSkinnable().getBarBackgroundColor());
        barBackground.setStroke(getSkinnable().getBorderPaint());
        barBackground.setEffect(getSkinnable().isShadowsEnabled() ? innerShadow : null);

        dataBarStart          = new MoveTo(0, 0.675 * PREFERRED_HEIGHT);
        dataBarOuterArc       = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, 0, 0, false, true);
        dataBarLineToInnerArc = new LineTo(0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT);
        dataBarInnerArc       = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0, 0, false, false);

        dataBar = new Path();
        dataBar.setFillRule(FillRule.EVEN_ODD);
        dataBar.getElements().add(dataBarStart);
        dataBar.getElements().add(dataBarOuterArc);
        dataBar.getElements().add(dataBarLineToInnerArc);
        dataBar.getElements().add(dataBarInnerArc);
        dataBar.getElements().add(new ClosePath());
        setBarColor(getSkinnable().getCurrentValue());
        dataBar.setStroke(getSkinnable().getBorderPaint());
        dataBar.setEffect(getSkinnable().isShadowsEnabled() ? innerShadow : null);

        threshold = new Line();
        threshold.setStrokeLineCap(StrokeLineCap.BUTT);
        threshold.setVisible(getSkinnable().isThresholdVisible());
        threshold.setManaged(getSkinnable().isThresholdVisible());

        thresholdText = new Text(String.format(locale, formatString, getSkinnable().getThreshold()));
        thresholdText.setVisible(getSkinnable().isThresholdVisible());
        thresholdText.setManaged(getSkinnable().isThresholdVisible());

        pane = new Pane(unitText, titleText, valueText, minText, maxText, barBackground, dataBar, threshold, thresholdText);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> resize());
        getSkinnable().heightProperty().addListener(o -> resize());
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> setBar(getSkinnable().getCurrentValue()));
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            range     = getSkinnable().getRange();
            angleStep = ANGLE_RANGE / range;
            minValue  = getSkinnable().getMinValue();
            sections  = getSkinnable().getSections();
            resize();
            redraw();
        } else if ("VISBILITY".equals(EVENT_TYPE)) {
            boolean isTitleVisible = !getSkinnable().getTitle().isEmpty();
            unitText.setVisible(isTitleVisible);
            unitText.setManaged(isTitleVisible);
            boolean isUnitVisible = !getSkinnable().getUnit().isEmpty();
            unitText.setVisible(isUnitVisible);
            unitText.setManaged(isUnitVisible);
            boolean isValueVisible = getSkinnable().isValueVisible();
            valueText.setVisible(isValueVisible);
            valueText.setManaged(isValueVisible);
            boolean tickLabelsVisible = getSkinnable().getTickLabelsVisible();
            minText.setVisible(tickLabelsVisible);
            minText.setManaged(tickLabelsVisible);
            maxText.setVisible(tickLabelsVisible);
            maxText.setManaged(tickLabelsVisible);
            boolean isThresholdVisible = getSkinnable().isThresholdVisible();
            threshold.setVisible(isThresholdVisible);
            threshold.setManaged(isThresholdVisible);
            thresholdText.setVisible(isThresholdVisible);
            thresholdText.setManaged(isThresholdVisible);

            redraw();
        }
    }


    // ******************** Private Methods ***********************************
    private void setBar(final double VALUE) {
        currentValueAngle = Helper.clamp(90d, 270d, (VALUE - minValue) * angleStep + 90d);
        dataBarOuterArc.setX(centerX + (0.675 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
        dataBarOuterArc.setY(centerX + (0.675 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
        dataBarLineToInnerArc.setX(centerX + (0.3 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
        dataBarLineToInnerArc.setY(centerX + (0.3 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
        setBarColor(VALUE);
        valueText.setText(String.format(locale, formatString, VALUE));
        if (valueText.getLayoutBounds().getWidth() > 0.28 * width) Helper.adjustTextSize(valueText, 0.28 * width, size * 0.24);
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, 0.615 * height + (0.3 * height - valueText.getLayoutBounds().getHeight()) * 0.5);
    }
    private void setBarColor(final double VALUE) {
        if (!sectionsVisible && !colorGradientEnabled) {
            dataBar.setFill(getSkinnable().getBarColor());
        } else if (colorGradientEnabled && noOfGradientStops > 1) {
            dataBar.setFill(getSkinnable().getGradientLookup().getColorAt((VALUE - minValue) / range));
        } else {
            for (Section section : sections) {
                if (section.contains(VALUE)) {
                    dataBar.setFill(section.getColor());
                    break;
                }
            }
        }
    }

    private void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size   = width < height ? width : height;

        if (ASPECT_RATIO * width > height) {
            width  = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - width) * 0.5, (getSkinnable().getHeight() - height) * 0.5);

            centerX   = width * 0.5;
            smallFont = Fonts.robotoThin(0.12 * height);
            bigFont   = Fonts.robotoRegular(0.24 * height);

            unitText.setFont(smallFont);
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, 0.5 * height);

            double maxWidth = 0.95 * width;
            titleText.setFont(smallFont);
            if (titleText.getLayoutBounds().getWidth() > maxWidth) Helper.adjustTextSize(titleText, maxWidth, size * 0.12);
            titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.88 * height);

            maxWidth = 0.28 * width;
            valueText.setFont(bigFont);
            if (valueText.getLayoutBounds().getWidth() > maxWidth) Helper.adjustTextSize(valueText, maxWidth, size * 0.24);
            valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, 0.615 * height + (0.3 * height - valueText.getLayoutBounds().getHeight()) * 0.5);

            maxWidth = 0.27  * width;
            minText.setText(String.format(locale, otherFormatString, minValue));
            minText.setFont(smallFont);
            if (minText.getLayoutBounds().getWidth() > maxWidth) Helper.adjustTextSize(minText, maxWidth, size * 0.12);
            minText.relocate(((0.27778 * width) - minText.getLayoutBounds().getWidth()) * 0.5, 0.7 * height);

            maxText.setText(String.format(locale, otherFormatString, getSkinnable().getMaxValue()));
            maxText.setFont(smallFont);
            if (maxText.getLayoutBounds().getWidth() > maxWidth) Helper.adjustTextSize(maxText, maxWidth, size * 0.12);
            maxText.relocate(((0.27778 * width) - maxText.getLayoutBounds().getWidth()) * 0.5 + 0.72222 * width, 0.7 * height);

            if (getSkinnable().isShadowsEnabled()) {
                innerShadow.setRadius(0.075 * height);
                innerShadow.setOffsetY(0.025 * height);
            }

            barBackgroundStart.setX(0);
            barBackgroundStart.setY(0.675 * height);
            barBackgroundOuterArc.setRadiusX(0.675 * height);
            barBackgroundOuterArc.setRadiusY(0.675 * height);
            barBackgroundOuterArc.setX(width);
            barBackgroundOuterArc.setY(0.675 * height);
            barBackgroundLineToInnerArc.setX(0.72222 * width);
            barBackgroundLineToInnerArc.setY(0.675 * height);
            barBackgroundInnerArc.setRadiusX(0.3 * height);
            barBackgroundInnerArc.setRadiusY(0.3 * height);
            barBackgroundInnerArc.setX(0.27778 * width);
            barBackgroundInnerArc.setY(0.675 * height);

            currentValueAngle = Helper.clamp(90d, 270d, (getSkinnable().getCurrentValue() - minValue) * angleStep + 90d);
            dataBarStart.setX(0);
            dataBarStart.setY(0.675 * height);
            dataBarOuterArc.setRadiusX(0.675 * height);
            dataBarOuterArc.setRadiusY(0.675 * height);
            dataBarOuterArc.setX(centerX + (0.675 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarOuterArc.setY(centerX + (0.675 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setX(centerX + (0.3 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setY(centerX + (0.3 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarInnerArc.setRadiusX(0.3 * height);
            dataBarInnerArc.setRadiusY(0.3 * height);
            dataBarInnerArc.setX(0.27778 * width);
            dataBarInnerArc.setY(0.675 * height);

            threshold.setStroke(getSkinnable().getThresholdColor());
            threshold.setStrokeWidth(Helper.clamp(1d, 2d, 0.00675676 * height));
            double thresholdInnerRadius = 0.3 * height;
            double thresholdOuterRadius = 0.675 * height;
            double thresholdAngle       = Helper.clamp(90d, 270d, (getSkinnable().getThreshold() - minValue) * angleStep + 90d);
            threshold.setStartX(centerX + thresholdInnerRadius * Math.sin(-Math.toRadians(thresholdAngle)));
            threshold.setStartY(centerX + thresholdInnerRadius * Math.cos(-Math.toRadians(thresholdAngle)));
            threshold.setEndX(centerX + thresholdOuterRadius * Math.sin(-Math.toRadians(thresholdAngle)));
            threshold.setEndY(centerX + thresholdOuterRadius * Math.cos(-Math.toRadians(thresholdAngle)));

            double thresholdTextRadius = 0.26 * height;
            thresholdText.setFill(getSkinnable().getValueColor());
            thresholdText.setText(String.format(locale, formatString, getSkinnable().getThreshold()));
            thresholdText.setFont(Fonts.robotoBold(size * 0.047));
            thresholdText.setRotate(thresholdAngle + 180);
            thresholdText.relocate(centerX - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.sin(-Math.toRadians(thresholdAngle)),
                                   centerX - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.cos(-Math.toRadians(thresholdAngle)));
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth() / 250 * size))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        colorGradientEnabled = getSkinnable().isGradientBarEnabled();
        noOfGradientStops    = getSkinnable().getGradientBarStops().size();
        sectionsVisible      = getSkinnable().getSectionsVisible();

        barBackground.setFill(getSkinnable().getBarBackgroundColor());
        barBackground.setEffect(getSkinnable().isShadowsEnabled() ? innerShadow : null);

        setBarColor(getSkinnable().getCurrentValue());

        dataBar.setEffect(getSkinnable().isShadowsEnabled() ? innerShadow : null);

        threshold.setStroke(getSkinnable().getThresholdColor());
        double thresholdInnerRadius = 0.3 * height;
        double thresholdOuterRadius = 0.675 * height;
        double thresholdAngle       = Helper.clamp(90d, 270d, (getSkinnable().getThreshold() - minValue) * angleStep + 90d);
        threshold.setStartX(centerX + thresholdInnerRadius * Math.sin(-Math.toRadians(thresholdAngle)));
        threshold.setStartY(centerX + thresholdInnerRadius * Math.cos(-Math.toRadians(thresholdAngle)));
        threshold.setEndX(centerX + thresholdOuterRadius * Math.sin(-Math.toRadians(thresholdAngle)));
        threshold.setEndY(centerX + thresholdOuterRadius * Math.cos(-Math.toRadians(thresholdAngle)));

        redrawText();
    }
    private void redrawText() {
        locale            = getSkinnable().getLocale();
        formatString      = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();
        otherFormatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getTickLabelDecimals())).append("f").toString();

        titleText.setFill(getSkinnable().getTitleColor());
        titleText.setText(getSkinnable().getTitle());
        titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.88 * height);

        valueText.setFill(getSkinnable().getValueColor());
        valueText.setText(String.format(locale, formatString, getSkinnable().getCurrentValue()));
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, 0.615 * height + (0.3 * height - valueText.getLayoutBounds().getHeight()) * 0.5);

        minText.setFill(getSkinnable().getValueColor());
        minText.setText(String.format(locale, otherFormatString, getSkinnable().getMinValue()));
        minText.relocate(((0.27778 * width) - minText.getLayoutBounds().getWidth()) * 0.5, 0.7 * height);

        maxText.setFill(getSkinnable().getValueColor());
        maxText.setText(String.format(locale, otherFormatString, getSkinnable().getMaxValue()));
        maxText.relocate(((0.27778 * width) - maxText.getLayoutBounds().getWidth()) * 0.5 + 0.72222 * width, 0.7 * height);

        unitText.setFill(getSkinnable().getUnitColor());
        unitText.setText(getSkinnable().getUnit());
        unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, 0.5 * height);

        double thresholdAngle      = Helper.clamp(90d, 270d, (getSkinnable().getThreshold() - minValue) * angleStep + 90d);
        double thresholdTextRadius = 0.26 * height;
        thresholdText.setFill(getSkinnable().getValueColor());
        thresholdText.setText(String.format(locale, formatString, getSkinnable().getThreshold()));
        thresholdText.setFont(Fonts.robotoBold(size * 0.047));
        thresholdText.setRotate(thresholdAngle + 180);
        thresholdText.relocate(centerX - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.sin(-Math.toRadians(thresholdAngle)),
                               centerX - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.cos(-Math.toRadians(thresholdAngle)));
    }

}
