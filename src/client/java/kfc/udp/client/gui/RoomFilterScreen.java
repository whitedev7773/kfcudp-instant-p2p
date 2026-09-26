package kfc.udp.client.gui;

import kfc.udp.client.webrtc.P2PConfig;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
*///?} else {
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
//?}

/** Draft filters are committed together; Escape and Cancel leave the list unchanged. */
public final class RoomFilterScreen extends Screen {
    private final RoomListScreen parent;
    private boolean joinableOnly;
    private int page;
    private static final int TAB_Y = 46;
    private static final int TAB_BOTTOM = 70;
    private static final int TAB_PANEL = 0xFF000000;
    private static final int TAB_BORDER = 0xFF555555;
    private static final int TAB_ACCENT = 0xFFA0A0A0;
    private int panelWidth() { return Math.min(420, this.width - 24); }
    private int panelBottom() { return Math.min(184, this.height - 58); }
    private int controlWidth() { return (panelWidth() - 24) / 2; }
    private int controlX() { return (this.width + panelWidth()) / 2 - 12 - controlWidth(); }
    private int[] numbers;
    private boolean sameVersionOnly;
    private static final int[][] CHOICES = {{0, 4, 8, 16, 32, 64, 100}, {0, 1, 2, 4, 8}};
    private static final String[] NUMBER_KEYS = {"capacity", "free_slots"};

    private RoomFilters extra() { return new RoomFilters(numbers[0], numbers[1], sameVersionOnly); }

    private int activeCount() {
        return extra().activeCount() + (joinableOnly ? 1 : 0)
                + (broadcast != P2PConfig.BroadcastFilter.ALL ? 1 : 0);
    }

    private void cycleNumber(int index) {
        int[] choices = CHOICES[index];
        int current = 0;
        while (current < choices.length && choices[current] != numbers[index]) current++;
        numbers[index] = choices[(current + 1) % choices.length];
    }
    private P2PConfig.BroadcastFilter broadcast = P2PConfig.getBroadcastFilter();

    public RoomFilterScreen(RoomListScreen parent, boolean joinableOnly, RoomFilters extra) {
        //? if >=26.1 {
        /*super(Component.translatable("instant-p2p.filters.title"));
        *///?} else {
        super(Text.translatable("instant-p2p.filters.title"));
        //?}
        this.parent = parent;
        this.joinableOnly = joinableOnly;
        this.numbers = new int[]{extra.maxCapacity(), extra.minFreeSlots()};
        this.sameVersionOnly = extra.sameVersionOnly();
    }

    private String broadcastKey() {
        return "instant-p2p.room_list.broadcast_filter." + switch (this.broadcast) {
            case ALL -> "all";
            case ALLOWED_ONLY -> "allowed_only";
            case DISALLOWED_ONLY -> "disallowed_only";
        };
    }

    //? if >=26.1 {
    /*private Button applyButton;
    private final java.util.List<Button> tabButtons = new java.util.ArrayList<>();

    @Override
    protected void init() {
        int w = this.panelWidth();
        int x = (this.width - w) / 2;
        this.addRenderableOnly((context, mouseX, mouseY, deltaTicks) ->
                context.fill(x, TAB_BOTTOM + 1, x + w, this.panelBottom(), 0xB0000000));
        String[] tabs = new String[]{"basic", "players"};
        this.tabButtons.clear();
        for (int i = 0; i < tabs.length; i++) {
            int selected = i;
            int left = x + i * w / tabs.length;
            int right = x + (i + 1) * w / tabs.length;
            var tab = Button.builder(Component.translatable("instant-p2p.filters.tab." + tabs[i]), clicked -> {
                if (this.page == selected) return;
                this.page = selected;
                this.rebuildWidgets();
                this.setInitialFocus(this.tabButtons.get(selected));
            }).bounds(left, this.page == i ? TAB_Y : TAB_Y + 3,
                    right - left - (i == tabs.length - 1 ? 0 : 4), this.page == i ? TAB_BOTTOM - TAB_Y : TAB_BOTTOM - TAB_Y - 3).build();
            // Register input/narration only. Alpha-zero vanilla buttons can still draw text
            // in some versions; the custom tab renderer must be the sole text renderer.
            this.tabButtons.add(tab);
            this.addWidget(tab);
        }
        if (this.page == 0) {
            this.addRenderableWidget(Button.builder(this.versionLabel(), clicked -> {
                this.sameVersionOnly = !this.sameVersionOnly;
                if (!this.sameVersionOnly) this.joinableOnly = false;
                clicked.setMessage(this.versionLabel());
            }).bounds(this.controlX(), 84, this.controlWidth(), 20)
                    .createNarration(value -> Component.translatable("instant-p2p.filters.toggle", Component.translatable("instant-p2p.filters.version"), value.get()))
                    .tooltip(Tooltip.create(Component.translatable("instant-p2p.filters.version_hint"))).build());
            this.addRenderableWidget(Button.builder(this.broadcastLabel(), clicked -> {
                var values = P2PConfig.BroadcastFilter.values();
                this.broadcast = values[(this.broadcast.ordinal() + 1) % values.length];
                clicked.setMessage(this.broadcastLabel());
            }).bounds(this.controlX(), 114, this.controlWidth(), 20)
                    .createNarration(value -> Component.translatable("instant-p2p.filters.toggle", Component.translatable("instant-p2p.room_list.broadcast_filter"), value.get())).build());
        } else {
            for (int i = 0; i < 2; i++) {
                int index = i;
                this.addRenderableWidget(Button.builder(this.numberLabel(index), clicked -> {
                    this.cycleNumber(index);
                    clicked.setMessage(this.numberLabel(index));
                    this.applyButton.active = this.extra().valid();
                }).bounds(this.controlX(), 84 + i * 30, this.controlWidth(), 20)
                    .createNarration(value -> Component.translatable("instant-p2p.filters.toggle", Component.translatable("instant-p2p.filters." + NUMBER_KEYS[index]), value.get()))
                        .tooltip(Tooltip.create(Component.translatable("instant-p2p.filters." + NUMBER_KEYS[index] + "_hint"))).build());
            }
            this.addRenderableWidget(Button.builder(this.toggleLabel("instant-p2p.room_list.joinable_only", this.joinableOnly), clicked -> {
                this.joinableOnly = !this.joinableOnly;
                if (this.joinableOnly) this.sameVersionOnly = true;
                clicked.setMessage(this.toggleLabel("instant-p2p.room_list.joinable_only", this.joinableOnly));
            }).bounds(this.controlX(), 144, this.controlWidth(), 20)
                    .createNarration(value -> Component.translatable("instant-p2p.filters.toggle", Component.translatable("instant-p2p.room_list.joinable_only"), value.get()))
                    .tooltip(Tooltip.create(Component.translatable("instant-p2p.filters.joinable_hint"))).build());
        }
        int y = this.panelBottom() + 30;
        int bw = Math.min(90, (w - 24) / 3);
        this.addRenderableWidget(Button.builder(Component.translatable("instant-p2p.filters.reset"), button -> {
            this.joinableOnly = false;
            this.sameVersionOnly = false;
            this.broadcast = P2PConfig.BroadcastFilter.ALL;
            this.numbers = new int[2];
            this.rebuildWidgets();
        }).bounds(x, y, bw, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
                .bounds(x + w - bw * 2 - 6, y, bw, 20).build());
        this.applyButton = Button.builder(Component.translatable("instant-p2p.filters.apply"), button -> {
            if (!this.extra().valid()) return;
            this.parent.applyFilters(this.joinableOnly, this.broadcast, this.extra());
            this.onClose();
        }).bounds(x + w - bw, y, bw, 20).build();
        this.applyButton.active = this.extra().valid();
        this.addRenderableWidget(this.applyButton);
    }

    private void renderTabs(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int w = this.panelWidth();
        int x = (this.width - w) / 2;
        int bottom = this.panelBottom();
        // A shared frame joins the active tab to its content without covering any widgets.
        context.fill(x, TAB_BOTTOM, x + w, TAB_BOTTOM + 1, TAB_BORDER);
        context.fill(x, TAB_BOTTOM + 1, x + 1, bottom, TAB_BORDER);
        context.fill(x + w - 1, TAB_BOTTOM + 1, x + w, bottom, TAB_BORDER);
        context.fill(x, bottom, x + w, bottom + 1, TAB_BORDER);
        // The translucent content background is drawn before the controls.
        for (int i = 0; i < this.tabButtons.size(); i++) {
            var tab = this.tabButtons.get(i);
            boolean selected = this.page == i;
            boolean focused = tab.isFocused();
            int left = tab.getX(), top = tab.getY(), right = left + tab.getWidth();
            boolean hovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < TAB_BOTTOM;
            int edge = focused ? 0xFFFFFFFF : selected ? TAB_ACCENT : TAB_BORDER;
            context.fill(left, top, right, TAB_BOTTOM + 1,
                    selected ? TAB_PANEL : hovered ? 0xFF181818 : 0xFF000000);
            context.fill(left, top, right, top + (selected || focused ? 2 : 1), edge);
            context.fill(left, top + 1, left + 1, TAB_BOTTOM, edge);
            context.fill(right - 1, top + 1, right, TAB_BOTTOM, edge);
            if (!selected) context.fill(left, TAB_BOTTOM, right, TAB_BOTTOM + 1, TAB_BORDER);
            context.centeredText(this.font, tab.getMessage(), (left + right) / 2,
                    top + (TAB_BOTTOM - top - 9) / 2,
                    selected || focused || hovered ? 0xFFFFFFFF : 0xFFA0A0A0);
        }
    }

    private void renderFormLabels(GuiGraphicsExtractor context) {
        int x = (this.width - this.panelWidth()) / 2 + 12;
        String[] keys = this.page == 0
                ? new String[]{"instant-p2p.filters.version", "instant-p2p.room_list.broadcast_filter"}
                : new String[]{"instant-p2p.filters.capacity", "instant-p2p.filters.free_slots", "instant-p2p.room_list.joinable_only"};
        for (int i = 0; i < keys.length; i++) {
            var lines = this.font.split(Component.translatable(keys[i]), this.controlX() - x - 8);
            int y = 84 + i * 30 + (20 - lines.size() * 9) / 2;
            for (var line : lines) {
                context.text(this.font, line, x, y, 0xFFE0E0E0);
                y += 9;
            }
        }
    }

    private Component versionLabel() {
        return this.toggleLabel("instant-p2p.filters.version", this.sameVersionOnly);
    }

    private Component numberLabel(int index) {
        Component value = numbers[index] == 0 ? Component.translatable("instant-p2p.filters.any")
                : Component.translatable(index == 0 ? "instant-p2p.filters.at_most" : "instant-p2p.filters.at_least", numbers[index]);
        return value;
    }

    private Component toggleLabel(String key, boolean enabled) {
        return Component.translatable(enabled ? "options.on" : "options.off");
    }

    private Component broadcastLabel() {
        return Component.translatable(this.broadcastKey() + ".short");
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreenAndShow(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
        this.renderTabs(context, mouseX, mouseY);
        this.renderFormLabels(context);
        context.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);
        context.centeredText(this.font, Component.translatable("instant-p2p.filters.hint"), this.width / 2, 30, 0xFFA0A0A0);
        context.fill((this.width - this.panelWidth()) / 2, this.panelBottom() + 5,
                (this.width + this.panelWidth()) / 2, this.panelBottom() + 6, 0xFF555555);
        context.centeredText(this.font, this.extra().valid()
                ? Component.translatable("instant-p2p.filters.summary", this.activeCount())
                : Component.translatable("instant-p2p.filters.invalid"), this.width / 2, this.panelBottom() + 13,
                this.extra().valid() ? 0xFFA0A0A0 : 0xFFFF5555);
    }
    *///?} else {
    private ButtonWidget applyButton;
    private final java.util.List<ButtonWidget> tabButtons = new java.util.ArrayList<>();

    @Override
    protected void init() {
        int w = this.panelWidth();
        int x = (this.width - w) / 2;
        this.addDrawable((context, mouseX, mouseY, deltaTicks) ->
                context.fill(x, TAB_BOTTOM + 1, x + w, this.panelBottom(), 0xB0000000));
        String[] tabs = new String[]{"basic", "players"};
        this.tabButtons.clear();
        for (int i = 0; i < tabs.length; i++) {
            int selected = i;
            int left = x + i * w / tabs.length;
            int right = x + (i + 1) * w / tabs.length;
            var tab = ButtonWidget.builder(Text.translatable("instant-p2p.filters.tab." + tabs[i]), clicked -> {
                if (this.page == selected) return;
                this.page = selected;
                this.clearAndInit();
                this.setInitialFocus(this.tabButtons.get(selected));
            }).dimensions(left, this.page == i ? TAB_Y : TAB_Y + 3,
                    right - left - (i == tabs.length - 1 ? 0 : 4), this.page == i ? TAB_BOTTOM - TAB_Y : TAB_BOTTOM - TAB_Y - 3).build();
            // Register input/narration only. Alpha-zero vanilla buttons can still draw text
            // in some versions; the custom tab renderer must be the sole text renderer.
            this.tabButtons.add(tab);
            this.addSelectableChild(tab);
        }
        if (this.page == 0) {
            this.addDrawableChild(ButtonWidget.builder(this.versionLabel(), clicked -> {
                this.sameVersionOnly = !this.sameVersionOnly;
                if (!this.sameVersionOnly) this.joinableOnly = false;
                clicked.setMessage(this.versionLabel());
            }).dimensions(this.controlX(), 84, this.controlWidth(), 20)
                    .narrationSupplier(value -> Text.translatable("instant-p2p.filters.toggle", Text.translatable("instant-p2p.filters.version"), value.get()))
                    .tooltip(Tooltip.of(Text.translatable("instant-p2p.filters.version_hint"))).build());
            this.addDrawableChild(ButtonWidget.builder(this.broadcastLabel(), clicked -> {
                var values = P2PConfig.BroadcastFilter.values();
                this.broadcast = values[(this.broadcast.ordinal() + 1) % values.length];
                clicked.setMessage(this.broadcastLabel());
            }).dimensions(this.controlX(), 114, this.controlWidth(), 20)
                    .narrationSupplier(value -> Text.translatable("instant-p2p.filters.toggle", Text.translatable("instant-p2p.room_list.broadcast_filter"), value.get())).build());
        } else {
            for (int i = 0; i < 2; i++) {
                int index = i;
                this.addDrawableChild(ButtonWidget.builder(this.numberLabel(index), clicked -> {
                    this.cycleNumber(index);
                    clicked.setMessage(this.numberLabel(index));
                    this.applyButton.active = this.extra().valid();
                }).dimensions(this.controlX(), 84 + i * 30, this.controlWidth(), 20)
                    .narrationSupplier(value -> Text.translatable("instant-p2p.filters.toggle", Text.translatable("instant-p2p.filters." + NUMBER_KEYS[index]), value.get()))
                        .tooltip(Tooltip.of(Text.translatable("instant-p2p.filters." + NUMBER_KEYS[index] + "_hint"))).build());
            }
            this.addDrawableChild(ButtonWidget.builder(this.toggleLabel("instant-p2p.room_list.joinable_only", this.joinableOnly), clicked -> {
                this.joinableOnly = !this.joinableOnly;
                if (this.joinableOnly) this.sameVersionOnly = true;
                clicked.setMessage(this.toggleLabel("instant-p2p.room_list.joinable_only", this.joinableOnly));
            }).dimensions(this.controlX(), 144, this.controlWidth(), 20)
                    .narrationSupplier(value -> Text.translatable("instant-p2p.filters.toggle", Text.translatable("instant-p2p.room_list.joinable_only"), value.get()))
                    .tooltip(Tooltip.of(Text.translatable("instant-p2p.filters.joinable_hint"))).build());
        }
        int y = this.panelBottom() + 30;
        int bw = Math.min(90, (w - 24) / 3);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("instant-p2p.filters.reset"), button -> {
            this.joinableOnly = false;
            this.sameVersionOnly = false;
            this.broadcast = P2PConfig.BroadcastFilter.ALL;
            this.numbers = new int[2];
            this.clearAndInit();
        }).dimensions(x, y, bw, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> this.close())
                .dimensions(x + w - bw * 2 - 6, y, bw, 20).build());
        this.applyButton = ButtonWidget.builder(Text.translatable("instant-p2p.filters.apply"), button -> {
            if (!this.extra().valid()) return;
            this.parent.applyFilters(this.joinableOnly, this.broadcast, this.extra());
            this.close();
        }).dimensions(x + w - bw, y, bw, 20).build();
        this.applyButton.active = this.extra().valid();
        this.addDrawableChild(this.applyButton);
    }

    private void renderTabs(DrawContext context, int mouseX, int mouseY) {
        int w = this.panelWidth();
        int x = (this.width - w) / 2;
        int bottom = this.panelBottom();
        // A shared frame joins the active tab to its content without covering any widgets.
        context.fill(x, TAB_BOTTOM, x + w, TAB_BOTTOM + 1, TAB_BORDER);
        context.fill(x, TAB_BOTTOM + 1, x + 1, bottom, TAB_BORDER);
        context.fill(x + w - 1, TAB_BOTTOM + 1, x + w, bottom, TAB_BORDER);
        context.fill(x, bottom, x + w, bottom + 1, TAB_BORDER);
        // The translucent content background is drawn before the controls.
        for (int i = 0; i < this.tabButtons.size(); i++) {
            var tab = this.tabButtons.get(i);
            boolean selected = this.page == i;
            boolean focused = tab.isFocused();
            int left = tab.getX(), top = tab.getY(), right = left + tab.getWidth();
            boolean hovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < TAB_BOTTOM;
            int edge = focused ? 0xFFFFFFFF : selected ? TAB_ACCENT : TAB_BORDER;
            context.fill(left, top, right, TAB_BOTTOM + 1,
                    selected ? TAB_PANEL : hovered ? 0xFF181818 : 0xFF000000);
            context.fill(left, top, right, top + (selected || focused ? 2 : 1), edge);
            context.fill(left, top + 1, left + 1, TAB_BOTTOM, edge);
            context.fill(right - 1, top + 1, right, TAB_BOTTOM, edge);
            if (!selected) context.fill(left, TAB_BOTTOM, right, TAB_BOTTOM + 1, TAB_BORDER);
            context.drawCenteredTextWithShadow(this.textRenderer, tab.getMessage(), (left + right) / 2,
                    top + (TAB_BOTTOM - top - 9) / 2,
                    selected || focused || hovered ? 0xFFFFFFFF : 0xFFA0A0A0);
        }
    }

    private void renderFormLabels(DrawContext context) {
        int x = (this.width - this.panelWidth()) / 2 + 12;
        String[] keys = this.page == 0
                ? new String[]{"instant-p2p.filters.version", "instant-p2p.room_list.broadcast_filter"}
                : new String[]{"instant-p2p.filters.capacity", "instant-p2p.filters.free_slots", "instant-p2p.room_list.joinable_only"};
        for (int i = 0; i < keys.length; i++) {
            var lines = this.textRenderer.wrapLines(Text.translatable(keys[i]), this.controlX() - x - 8);
            int y = 84 + i * 30 + (20 - lines.size() * 9) / 2;
            for (var line : lines) {
                context.drawTextWithShadow(this.textRenderer, line, x, y, 0xFFE0E0E0);
                y += 9;
            }
        }
    }

    private Text versionLabel() {
        return this.toggleLabel("instant-p2p.filters.version", this.sameVersionOnly);
    }

    private Text numberLabel(int index) {
        Text value = numbers[index] == 0 ? Text.translatable("instant-p2p.filters.any")
                : Text.translatable(index == 0 ? "instant-p2p.filters.at_most" : "instant-p2p.filters.at_least", numbers[index]);
        return value;
    }

    private Text toggleLabel(String key, boolean enabled) {
        return Text.translatable(enabled ? "options.on" : "options.off");
    }

    private Text broadcastLabel() {
        return Text.translatable(this.broadcastKey() + ".short");
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.renderTabs(context, mouseX, mouseY);
        this.renderFormLabels(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("instant-p2p.filters.hint"), this.width / 2, 30, 0xFFA0A0A0);
        context.fill((this.width - this.panelWidth()) / 2, this.panelBottom() + 5,
                (this.width + this.panelWidth()) / 2, this.panelBottom() + 6, 0xFF555555);
        context.drawCenteredTextWithShadow(this.textRenderer, this.extra().valid()
                ? Text.translatable("instant-p2p.filters.summary", this.activeCount())
                : Text.translatable("instant-p2p.filters.invalid"), this.width / 2, this.panelBottom() + 13,
                this.extra().valid() ? 0xFFA0A0A0 : 0xFFFF5555);
    }
    //?}
}
