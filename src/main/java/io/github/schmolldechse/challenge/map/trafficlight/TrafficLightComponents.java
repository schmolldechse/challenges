package io.github.schmolldechse.challenge.map.trafficlight;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public class TrafficLightComponents {

    /**
     * Space characters to move elements on the x-axis
     */
    public final Component spaceNegative1 = Component.text("\uF008").font(Key.key("challenge", "space"));
    public final Component spaceNegative251 = Component.text("\uF300").font(Key.key("challenge", "space"));

    /**
     * Traffic light elements
     */
    public final Component trafficLight = Component.empty()
            .append(Component.text("A").font(Key.key("challenge", "trafficlight")))
            .append(this.spaceNegative1)
            .append(Component.text("B").font(Key.key("challenge", "trafficlight")))
            .append(this.spaceNegative1)
            .append(Component.text("C").font(Key.key("challenge", "trafficlight")));

    //TODO: fix moving elements 10px to the right when applying a color
    public final Component redLight = Component.empty()
            .append(Component.text("\uE201").font(Key.key("challenge", "trafficlight")))
            .append(this.spaceNegative1)
            .append(Component.text("\uE202").font(Key.key("challenge", "trafficlight")))
            .append(this.spaceNegative1)
            .append(Component.text("\uE203").font(Key.key("challenge", "trafficlight")));

    public final Component yellowLight = Component.empty()
            .append(Component.text("\uE211").font(Key.key("challenge", "trafficlight")))
            .append(this.spaceNegative1)
            .append(Component.text("\uE212").font(Key.key("challenge", "trafficlight")))
            .append(this.spaceNegative1)
            .append(Component.text("\uE213").font(Key.key("challenge", "trafficlight")));

    public final Component greenLight = Component.empty()
            .append(Component.text("\uE221").font(Key.key("challenge", "trafficlight")))
            .append(this.spaceNegative1)
            .append(Component.text("\uE222").font(Key.key("challenge", "trafficlight")))
            .append(this.spaceNegative1)
            .append(Component.text("\uE223").font(Key.key("challenge", "trafficlight")));
}
