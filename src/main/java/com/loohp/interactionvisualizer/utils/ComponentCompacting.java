/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2017-2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2017-2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.loohp.interactionvisualizer.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class ComponentCompacting {

    private static final TextDecoration[] DECORATIONS = TextDecoration.values();

    public static Component optimize(Component component) {
        return optimizeStyle(optimizeEvents(component), null);
    }

    public static Component optimizeEvents(Component component) {
        component = ComponentFlattening.flatten(component);
        List<Component> children = component.children();
        if (children.isEmpty()) {
            return component;
        }
        List<Component> optimized = new ArrayList<>();
        HoverEvent<?> hoverEvent = children.get(0).hoverEvent();
        ClickEvent clickEvent = children.get(0).clickEvent();
        Component currentComponent = Component.text("").hoverEvent(hoverEvent).clickEvent(clickEvent);
        for (int i = 0; i < children.size(); i++) {
            Component child = children.get(i);
            HoverEvent<?> childHover = child.hoverEvent();
            ClickEvent childClick = child.clickEvent();
            if (Objects.equals(hoverEvent, childHover) && Objects.equals(clickEvent, childClick)) {
                currentComponent = currentComponent.append(child.hoverEvent(null).clickEvent(null));
            } else {
                optimized.add(currentComponent);
                hoverEvent = childHover;
                clickEvent = childClick;
                currentComponent = Component.text("").hoverEvent(hoverEvent).clickEvent(clickEvent).append(child.hoverEvent(null).clickEvent(null));
            }
        }
        optimized.add(currentComponent);
        return component.children(optimized);
    }

    public static @NonNull Component optimizeStyle(final @NotNull Component component, final @Nullable Style parentStyle) {
        Component optimized = component.children(Collections.emptyList());

        if (parentStyle != null) {
            optimized = optimized.style(simplifyStyle(component.style(), parentStyle));
        }

        // propagate the parent style context to children
        // by merging this component's style into the parent style
        Style childParentStyle = optimized.style();
        if (parentStyle != null) {
            childParentStyle = parentStyle.merge(childParentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
        }

        // optimize all children
        final List<Component> childrenToAppend = new ArrayList<>(component.children().size());
        for (int i = 0; i < component.children().size(); ++i) {
            childrenToAppend.add(optimizeStyle(component.children().get(i), childParentStyle));
        }

        // try to merge children into this parent component
        for (final ListIterator<Component> it = childrenToAppend.listIterator(); it.hasNext(); ) {
            final Component child = it.next();
            final Style childStyle = child.style().merge(childParentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);

            if (optimized instanceof TextComponent && child instanceof TextComponent && Objects.equals(childStyle, childParentStyle) && optimized.children().isEmpty()) {
                // merge child components into the parent if they are a text component with the
                // same effective style
                // in context of their parent style
                optimized = concatContentWithFirstStyleAndSecondChildren((TextComponent) optimized, (TextComponent) child);
                it.remove();

                // if the merged child had any children, retain them
                child.children().forEach(it::add);
            } else {
                // this child can't be merged into the parent, so all children from now on must
                // remain children
                break;
            }
        }

        // try to concatenate any further children with their neighbor
        // until no further joining is possible
        for (int i = 0; i + 1 < childrenToAppend.size(); ) {
            final Component child = childrenToAppend.get(i);
            final Component neighbor = childrenToAppend.get(i + 1);

            // calculate the children's styles in context of their parent style
            final Style childStyle = child.style().merge(childParentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
            final Style neighborStyle = neighbor.style().merge(childParentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);

            if (child instanceof TextComponent && neighbor instanceof TextComponent && childStyle.equals(neighborStyle) && child.children().isEmpty()) {
                final Component combined = concatContentWithFirstStyleAndSecondChildren((TextComponent) child, (TextComponent) neighbor);

                // replace the child and its neighbor with the single, combined component
                childrenToAppend.set(i, combined);
                childrenToAppend.remove(i + 1);

                // don't increment the index -
                // we want to try and optimize this combined component even further
            } else {
                i++;
            }
        }

        return optimized.children(childrenToAppend);
    }

    /**
     * Simplify the provided style to remove any information that is redundant.
     *
     * @param style       style to simplify
     * @param parentStyle parent to compare against
     * @return a new, simplified style
     */
    private static @NonNull Style simplifyStyle(final @NonNull Style style, final @NonNull Style parentStyle) {
        final Style.Builder builder = style.toBuilder();

        if (Objects.equals(style.font(), parentStyle.font())) {
            builder.font(null);
        }

        if (Objects.equals(style.color(), parentStyle.color())) {
            builder.color(null);
        }

        for (int i = 0, length = DECORATIONS.length; i < length; i++) {
            final TextDecoration decoration = DECORATIONS[i];
            if (style.decoration(decoration) == parentStyle.decoration(decoration)) {
                builder.decoration(decoration, TextDecoration.State.NOT_SET);
            }
        }

        if (Objects.equals(style.clickEvent(), parentStyle.clickEvent())) {
            builder.clickEvent(null);
        }

        if (Objects.equals(style.hoverEvent(), parentStyle.hoverEvent())) {
            builder.hoverEvent(null);
        }

        if (Objects.equals(style.insertion(), parentStyle.insertion())) {
            builder.insertion(null);
        }

        return builder.build();
    }

    // todo(kashike): extract

    private static TextComponent concatContentWithFirstStyleAndSecondChildren(final TextComponent one, final TextComponent two) {
        return Component.text(one.content() + two.content(), one.style()).children(two.children());
    }

    private ComponentCompacting() {

    }

}