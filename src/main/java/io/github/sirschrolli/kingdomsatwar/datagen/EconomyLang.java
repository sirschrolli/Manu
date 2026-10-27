package io.github.sirschrolli.kingdomsatwar.datagen;

/**
 * Übersetzungen der Wirtschaft (Münzen, Stadt, Bürger, Handel, Ansehen, Krönung) – Englisch und Deutsch.
 */
final class EconomyLang {
    /** Ein Eintrag: Schlüssel, Englisch, Deutsch. */
    interface Text {
        void add(String key, String english, String deutsch);
    }

    private EconomyLang() {}

    static void register(Text t) {
        coins(t);
        town(t);
        professions(t);
        buildings(t);
        townScreen(t);
        messages(t);
        trade(t);
        crown(t);
    }

    private static void coins(Text t) {
        t.add("item.kingdomsatwar.bronze_coin", "Bronze Coin", "Bronzemünze");
        t.add("item.kingdomsatwar.silver_coin", "Silver Coin", "Silbermünze");
        t.add("item.kingdomsatwar.gold_coin", "Gold Coin", "Goldmünze");
        t.add("coin.kingdomsatwar.bronze", "%s Bronze", "%s Bronze");
        t.add("coin.kingdomsatwar.silver", "%s Silver", "%s Silber");
        t.add("coin.kingdomsatwar.gold", "%s Gold", "%s Gold");
        t.add("item.kingdomsatwar.coin.value", "Worth %s", "Wert: %s");
        t.add("item.kingdomsatwar.coin.stack_value", "Stack worth %s", "Stapel: %s");
        t.add("item.kingdomsatwar.coin.combine", "Use: change 10 into one %s", "Rechtsklick: 10 in eine %s wechseln");
        t.add("item.kingdomsatwar.coin.split", "Sneak + use: change one into ten smaller coins",
                "Schleichen + Rechtsklick: eine in zehn kleinere wechseln");
        t.add("message.kingdomsatwar.coin.need_ten", "You need ten of them to exchange", "Zum Wechseln brauchst du zehn Stück");
        t.add("gui.kingdomsatwar.recruitment.pay", "Pay: %s", "Sold: %s");
        t.add("gui.kingdomsatwar.recruitment.purse", "Your purse: %s", "Deine Börse: %s");
    }

    private static void town(Text t) {
        t.add("block.kingdomsatwar.town_banner", "Town Banner", "Stadtbanner");
        t.add("lore.kingdomsatwar.town_banner.1", "Place it to found your settlement.", "Aufstellen gründet deine Siedlung.");
        t.add("lore.kingdomsatwar.town_banner.2", "Use it to hire citizens and take blueprints.",
                "Rechtsklick: Bürger anheuern und Baupläne nehmen.");
        t.add("lore.kingdomsatwar.town_banner.3", "Only you can take your banner down.", "Nur du kannst dein Banner abbauen.");
        t.add("entity.kingdomsatwar.citizen", "Citizen", "Bürger");

        t.add("item.kingdomsatwar.blueprint", "Blueprint", "Bauplan");
        t.add("item.kingdomsatwar.blueprint.of", "Blueprint: %s", "Bauplan: %s");
        t.add("item.kingdomsatwar.blueprint.price", "Cost: %s", "Kosten: %s");
        t.add("item.kingdomsatwar.blueprint.size", "Size: %s × %s blocks", "Größe: %s × %s Blöcke");
        t.add("item.kingdomsatwar.blueprint.beds", "Beds: %s", "Betten: %s");
        t.add("item.kingdomsatwar.blueprint.use", "Use on the ground: set up the building site (entrance faces you)",
                "Rechtsklick auf den Boden: Baustelle einrichten (der Eingang zeigt zu dir)");
        t.add("item.kingdomsatwar.blueprint.turn", "Sneak + use: turn the plan", "Schleichen + Rechtsklick: Plan drehen");
        t.add("item.kingdomsatwar.map.seal_circle", "Map to the Seal Circle", "Karte zum Siegelkreis");
        t.add("item.kingdomsatwar.map.nest", "Map to a Dragon's Nest", "Karte zu einem Drachennest");

        t.add("town.kingdomsatwar.tier.camp", "Camp", "Lager");
        t.add("town.kingdomsatwar.tier.hamlet", "Hamlet", "Weiler");
        t.add("town.kingdomsatwar.tier.village", "Village", "Dorf");
        t.add("town.kingdomsatwar.tier.market_town", "Market Town", "Marktflecken");
        t.add("town.kingdomsatwar.tier.city", "City", "Stadt");

        t.add("reputation.kingdomsatwar.stranger", "Stranger", "Fremder");
        t.add("reputation.kingdomsatwar.known", "Known", "Bekannt");
        t.add("reputation.kingdomsatwar.respected", "Respected", "Geachtet");
        t.add("reputation.kingdomsatwar.honored", "Honored", "Geehrt");
        t.add("reputation.kingdomsatwar.celebrated", "Celebrated", "Gefeiert");
        t.add("reputation.kingdomsatwar.legendary", "Legendary", "Legendär");

        t.add("citizen.kingdomsatwar.activity.sleeping", "asleep", "schläft");
        t.add("citizen.kingdomsatwar.activity.working", "at work", "bei der Arbeit");
        t.add("citizen.kingdomsatwar.activity.idle", "taking a break", "macht Pause");

        t.add("kingdomsatwar.configuration.tradesBeforePause", "Trades before a shop pauses", "Handel bis zur Pause eines Ladens");
        t.add("kingdomsatwar.configuration.tradesBeforePause.tooltip",
                "How often you can trade with a citizen before their shop closes for a while.",
                "Wie oft man mit einem Bürger handeln kann, bevor sein Laden eine Weile schließt.");
        t.add("kingdomsatwar.configuration.tradePauseMinutes", "Shop pause (minutes, real time)", "Pause eines Ladens (Minuten, Echtzeit)");
        t.add("kingdomsatwar.configuration.tradePauseMinutes.tooltip",
                "How long a shop stays closed after the trade limit is reached – in real minutes, also while the game is closed.",
                "Wie lange ein Laden nach Erreichen der Handelsgrenze geschlossen bleibt – in echten Minuten, auch wenn das Spiel aus ist.");
    }

    private static void professions(Text t) {
        prof(t, "builder", "Builder", "Baumeister",
                "Builds every building of your settlement block by block from its blueprint. Needs no workshop.",
                "Baut jedes Gebäude deiner Siedlung Block für Block nach dem Bauplan. Braucht keine Werkstatt.");
        prof(t, "farmer", "Farmer", "Bauer",
                "Harvests ripe crops on his field, sows again right away and tends the plants; the harvest goes into the chest at the field.",
                "Erntet reife Feldfrüchte auf seinem Feld, sät gleich wieder und pflegt die Pflanzen; die Ernte kommt in die Truhe am Feld.");
        prof(t, "lumberjack", "Lumberjack", "Holzfäller",
                "Fells wild trees around his hut, plants a sapling for each and stacks the wood in the hut's chest.",
                "Fällt wilde Bäume rund um seine Hütte, pflanzt für jeden einen Setzling und bringt das Holz in die Truhe der Hütte.");
        prof(t, "miner", "Miner", "Bergmann",
                "Climbs down the ladder of the mine and works the ore wall: coal, iron, copper, rarely gold and gems.",
                "Steigt die Leiter der Mine hinab und hackt an der Erzwand: Kohle, Eisen, Kupfer, selten Gold und Edelsteine.");
        prof(t, "smith", "Smith", "Schmied",
                "Hammers at the anvil of the smithy and sells steel weapons and armor; buys iron, coal and gold.",
                "Hämmert am Amboss der Schmiede und verkauft Waffen und Rüstungen aus Stahl; kauft Eisen, Kohle und Gold.");
        prof(t, "merchant", "Merchant", "Händler",
                "Stands at the market: buys your harvest, wood and stone, sells building goods, emeralds and useful things.",
                "Steht am Markt: kauft deine Ernte, Holz und Stein und verkauft Baustoffe, Smaragde und Nützliches.");
        prof(t, "innkeeper", "Innkeeper", "Wirt",
                "Runs the tavern: sells bread, stews, pies, ale, wine and mead – and the great feast.",
                "Führt die Taverne: verkauft Brot, Eintopf, Pasteten, Bier, Wein und Met – und das große Festmahl.");
        prof(t, "cartographer", "Cartographer", "Kartograf",
                "Draws maps to the Seal Circle and to the nests of dragons not yet defeated; sells compasses and spyglasses.",
                "Zeichnet Karten zum Siegelkreis und zu den Nestern noch unbesiegter Drachen; verkauft Kompasse und Fernrohre.");
    }

    private static void prof(Text t, String id, String en, String de, String descEn, String descDe) {
        t.add("profession.kingdomsatwar." + id, en, de);
        t.add("profession.kingdomsatwar." + id + ".description", descEn, descDe);
    }

    private static void buildings(Text t) {
        bld(t, "house", "House", "Wohnhaus",
                "Half-timbered house on a stone base with a red gable roof and a chimney. Two beds.",
                "Fachwerkhaus auf Steinsockel mit rotem Satteldach und Kamin. Zwei Betten.");
        bld(t, "field", "Field", "Feld",
                "9 × 9 farmland with wheat, carrots, potatoes and beetroot, a fence, a scarecrow and a chest. Workplace of a farmer.",
                "9 × 9 Ackerland mit Weizen, Karotten, Kartoffeln und Roter Bete, Zaun, Vogelscheuche und Truhe. Arbeitsplatz eines Bauern.");
        bld(t, "lumber_hut", "Lumber Hut", "Holzfällerhütte",
                "Log cabin with a chopping block, a woodpile and a sapling nursery. One bed; workplace of a lumberjack.",
                "Blockhaus mit Hackklotz, Holzstapel und Setzlingsschule. Ein Bett; Arbeitsplatz eines Holzfällers.");
        bld(t, "well", "Well", "Brunnen",
                "Stone well with a tiled roof – the heart of every hamlet.",
                "Steinbrunnen mit Ziegeldach – das Herz jedes Weilers.");
        bld(t, "smithy", "Smithy", "Schmiede",
                "Stone workshop with forge, anvil and grindstone, the smith's chamber at the back. One bed; workplace of a smith.",
                "Steinerne Werkstatt mit Esse, Amboss und Schleifstein, hinten die Kammer des Schmieds. Ein Bett; Arbeitsplatz eines Schmieds.");
        bld(t, "mine", "Mine", "Mine",
                "Pithead over a ladder shaft ten blocks deep, at the bottom a chamber at the ore wall. Workplace of two miners.",
                "Stollenhaus über einem zehn Blöcke tiefen Leiterschacht, unten eine Kammer an der Erzwand. Arbeitsplatz zweier Bergleute.");
        bld(t, "market", "Market", "Markt",
                "Two market stalls with striped awnings around a paved square. Workplace of two merchants.",
                "Zwei Marktstände mit gestreiften Markisen um einen gepflasterten Platz. Arbeitsplatz zweier Händler.");
        bld(t, "tavern", "Tavern", "Taverne",
                "Two-storey inn with a taproom, a barrel bar, tables and two guest rooms. Two beds; workplace of an innkeeper.",
                "Zweistöckiges Gasthaus mit Schankstube, Theke aus Fässern, Tischen und zwei Kammern. Zwei Betten; Arbeitsplatz eines Wirts.");
        bld(t, "townhouse", "Townhouse", "Bürgerhaus",
                "Tall, narrow half-timbered house with an overhanging upper floor. Four beds.",
                "Schmales, hohes Fachwerkhaus mit vorkragendem Obergeschoss. Vier Betten.");
        bld(t, "barracks", "Barracks", "Kaserne",
                "Fortified stone house with weapon racks and a training yard. Every barracks lets you recruit eight more soldiers.",
                "Wehrhaftes Steinhaus mit Waffenständern und Übungsplatz. Jede Kaserne erlaubt dir acht Soldaten mehr.");
        bld(t, "map_house", "Map House", "Kartenhaus",
                "Slender tower with a map table, a lectern and a copper roof. One bed; workplace of a cartographer.",
                "Schlanker Turm mit Kartentisch, Lesepult und Kupferdach. Ein Bett; Arbeitsplatz eines Kartografen.");
        bld(t, "throne_hall", "Throne Hall", "Thronsaal",
                "Great limestone hall with stained glass, columns and a red carpet up to the throne. Here you are crowned.",
                "Große Halle aus Kalkstein mit Bleiglasfenstern, Säulen und rotem Teppich bis zum Thron. Hier wirst du gekrönt.");
    }

    private static void bld(Text t, String id, String en, String de, String descEn, String descDe) {
        t.add("building.kingdomsatwar." + id, en, de);
        t.add("building.kingdomsatwar." + id + ".description", descEn, descDe);
    }

    private static void townScreen(Text t) {
        t.add("gui.kingdomsatwar.town.tab.overview", "Town", "Siedlung");
        t.add("gui.kingdomsatwar.town.tab.hire", "Hire", "Anheuern");
        t.add("gui.kingdomsatwar.town.tab.residents", "Residents", "Bewohner");
        t.add("gui.kingdomsatwar.town.tab.build", "Build", "Bauen");
        t.add("gui.kingdomsatwar.town.tab.crown", "Crown", "Krone");
        t.add("gui.kingdomsatwar.town.name_hint", "Name of the settlement", "Name der Siedlung");
        t.add("gui.kingdomsatwar.town.rename", "Rename the settlement", "Siedlung umbenennen");
        t.add("gui.kingdomsatwar.town.foreign", "This is the settlement of %s.", "Dies ist die Siedlung von %s.");
        t.add("gui.kingdomsatwar.town.tier", "Rank", "Stufe");
        t.add("gui.kingdomsatwar.town.next_tier", "%s residents · %s to %s", "%s Bewohner · %s bis %s");
        t.add("gui.kingdomsatwar.town.max_tier", "%s residents · the highest rank", "%s Bewohner · die höchste Stufe");
        t.add("gui.kingdomsatwar.town.beds", "Beds: %s of %s taken", "Betten: %s von %s belegt");
        t.add("gui.kingdomsatwar.town.radius", "Land: %s blocks around", "Umkreis: %s Blöcke");
        t.add("gui.kingdomsatwar.town.owner", "Lord: %s", "Herr: %s");
        t.add("gui.kingdomsatwar.town.reputation", "Fame", "Ansehen");
        t.add("gui.kingdomsatwar.town.reputation_next", "%s points · %s to %s", "%s Punkte · %s bis %s");
        t.add("gui.kingdomsatwar.town.reputation_max", "%s points · the highest fame", "%s Punkte · höchstes Ansehen");
        t.add("gui.kingdomsatwar.town.discount", "Citizen discount: %s %%", "Bürger-Rabatt: %s %%");
        t.add("gui.kingdomsatwar.town.all_free", "Everything is free for you", "Für dich ist alles umsonst");
        t.add("gui.kingdomsatwar.town.projects", "Building sites", "Baustellen");
        t.add("gui.kingdomsatwar.town.builders", "Builders: %s", "Baumeister: %s");
        t.add("gui.kingdomsatwar.town.no_projects", "No building sites. Take a blueprint under “Build” and use it on the ground.",
                "Keine Baustellen. Nimm unter „Bauen“ einen Bauplan und setze ihn auf den Boden.");
        t.add("gui.kingdomsatwar.town.no_builder", "Without a builder nothing gets built – hire one!",
                "Ohne Baumeister wird nichts gebaut – heuere einen an!");
        t.add("gui.kingdomsatwar.town.cancel", "Give up this building site", "Baustelle aufgeben");
        t.add("gui.kingdomsatwar.town.cancel.refund", "Full refund if nothing is built yet, otherwise half",
                "Volles Geld zurück, solange nichts gebaut ist, sonst die Hälfte");
        t.add("gui.kingdomsatwar.town.purse", "Your purse:", "Deine Börse:");
        t.add("gui.kingdomsatwar.town.free_beds", "Free beds: %s", "Freie Betten: %s");
        t.add("gui.kingdomsatwar.town.free", "free", "umsonst");
        t.add("gui.kingdomsatwar.town.hire", "Hire", "Anheuern");
        t.add("gui.kingdomsatwar.town.hire.needs", "Needs a finished %s with a free workplace", "Braucht ein fertiges Gebäude „%s“ mit freiem Arbeitsplatz");
        t.add("gui.kingdomsatwar.town.hire.builder_limit", "No more builders: one per settlement, two from a village on",
                "Keine weiteren Baumeister: einer je Siedlung, ab dem Dorf zwei");
        t.add("gui.kingdomsatwar.town.hire.no_bed", "No free bed – build a house", "Kein freies Bett – baue ein Haus");
        t.add("gui.kingdomsatwar.town.hire.not_enough", "Not enough coins", "Nicht genug Münzen");
        t.add("gui.kingdomsatwar.town.hire.workplace", "Works at: %s", "Arbeitet in: %s");
        t.add("gui.kingdomsatwar.town.hire.trader", "Trades with you (use the citizen)", "Handelt mit dir (Rechtsklick auf den Bürger)");
        t.add("gui.kingdomsatwar.town.residents.name", "Name", "Name");
        t.add("gui.kingdomsatwar.town.residents.profession", "Profession", "Beruf");
        t.add("gui.kingdomsatwar.town.residents.state", "Now", "Gerade");
        t.add("gui.kingdomsatwar.town.residents.empty", "Nobody lives here yet. Hire citizens under “Hire”.",
                "Noch wohnt niemand hier. Heuere unter „Anheuern“ Bürger an.");
        t.add("gui.kingdomsatwar.town.residents.bed", "Has a bed", "Hat ein Bett");
        t.add("gui.kingdomsatwar.town.residents.no_bed", "No bed yet – sleeps at the banner", "Noch kein Bett – schläft am Banner");
        t.add("gui.kingdomsatwar.town.residents.work", "Workplace: %s", "Arbeitsplatz: %s");
        t.add("gui.kingdomsatwar.town.residents.no_work", "No workplace yet (%s)", "Noch kein Arbeitsplatz (%s)");
        t.add("gui.kingdomsatwar.town.state.0", "out and about", "unterwegs");
        t.add("gui.kingdomsatwar.town.state.1", "working", "arbeitet");
        t.add("gui.kingdomsatwar.town.state.2", "asleep", "schläft");
        t.add("gui.kingdomsatwar.town.state.3", "far away", "weit weg");
        t.add("gui.kingdomsatwar.town.dismiss", "Dismiss %s", "%s entlassen");
        t.add("gui.kingdomsatwar.town.dismiss_confirm", "Click again to dismiss %s", "Nochmal klicken, um %s zu entlassen");
        t.add("gui.kingdomsatwar.town.build.hint", "Click a building to take its blueprint.", "Klicke einen Bau an, um seinen Bauplan zu nehmen.");
        t.add("gui.kingdomsatwar.town.build.foreign", "Only the lord of the settlement can build here.",
                "Nur der Herr der Siedlung kann hier bauen.");
        t.add("gui.kingdomsatwar.town.build.locked", "from %s", "ab %s");
        t.add("gui.kingdomsatwar.town.build.needs_tier", "Needs the rank: %s", "Braucht die Stufe: %s");
        t.add("gui.kingdomsatwar.town.build.size", "Size: %s × %s blocks", "Größe: %s × %s Blöcke");
        t.add("gui.kingdomsatwar.town.build.beds", "Beds: %s", "Betten: %s");
        t.add("gui.kingdomsatwar.town.build.jobs", "Workplaces: %s × %s", "Arbeitsplätze: %s × %s");
        t.add("gui.kingdomsatwar.town.build.take", "Click: take the blueprint", "Klicken: Bauplan nehmen");
        t.add("gui.kingdomsatwar.town.notice.hired", "A new %s has arrived!", "Ein neuer %s ist da!");
        t.add("gui.kingdomsatwar.town.notice.renamed", "The settlement is now called %s", "Die Siedlung heißt jetzt %s");
        t.add("gui.kingdomsatwar.town.notice.dismissed", "%s has left the settlement", "%s hat die Siedlung verlassen");
        t.add("gui.kingdomsatwar.town.notice.cancelled", "Building site given up: %s", "Baustelle aufgegeben: %s");
    }

    private static void messages(Text t) {
        t.add("message.kingdomsatwar.town.founded", "You founded %s! Use the banner to hire citizens.",
                "Du hast %s gegründet! Rechtsklick auf das Banner: Bürger anheuern.");
        t.add("message.kingdomsatwar.town.found.founded", "Settlement founded", "Siedlung gegründet");
        t.add("message.kingdomsatwar.town.found.moved", "The banner of %s stands again", "Das Banner von %s steht wieder");
        t.add("message.kingdomsatwar.town.found.has_banner", "%s already has its banner at %s, %s, %s",
                "%s hat sein Banner schon bei %s, %s, %s");
        t.add("message.kingdomsatwar.town.found.too_close", "Too close to another settlement", "Zu nah an einer anderen Siedlung");
        t.add("message.kingdomsatwar.town.banner_removed", "The banner of %s has fallen – put it up again to keep ruling",
                "Das Banner von %s ist gefallen – stelle es wieder auf, um weiter zu herrschen");
        t.add("message.kingdomsatwar.town.no_town", "This banner belongs to no settlement", "Dieses Banner gehört zu keiner Siedlung");
        t.add("message.kingdomsatwar.town.hired", "%s, %s, joins your settlement", "%s, %s, zieht in deine Siedlung");
        t.add("message.kingdomsatwar.town.hire.hired", "Hired", "Angeheuert");
        t.add("message.kingdomsatwar.town.hire.not_owner", "Only the lord of the settlement can hire", "Nur der Herr der Siedlung kann anheuern");
        t.add("message.kingdomsatwar.town.hire.no_banner", "The banner must stand", "Das Banner muss stehen");
        t.add("message.kingdomsatwar.town.hire.no_workplace", "No free workplace for this profession", "Kein freier Arbeitsplatz für diesen Beruf");
        t.add("message.kingdomsatwar.town.hire.no_bed", "No free bed – build a house", "Kein freies Bett – baue ein Haus");
        t.add("message.kingdomsatwar.town.hire.not_enough", "Not enough coins", "Nicht genug Münzen");
        t.add("message.kingdomsatwar.town.hire.no_space", "No room next to the banner", "Kein Platz neben dem Banner");
        t.add("message.kingdomsatwar.town.blueprint_taken", "Blueprint taken: %s", "Bauplan genommen: %s");
        t.add("message.kingdomsatwar.town.place.placed", "Building site set up", "Baustelle eingerichtet");
        t.add("message.kingdomsatwar.town.place.not_owner", "Only the lord of the settlement can build", "Nur der Herr der Siedlung kann bauen");
        t.add("message.kingdomsatwar.town.place.no_town", "Found a settlement first (town banner)", "Gründe zuerst eine Siedlung (Stadtbanner)");
        t.add("message.kingdomsatwar.town.place.outside", "Outside your settlement – build closer to the banner",
                "Außerhalb deiner Siedlung – baue näher am Banner");
        t.add("message.kingdomsatwar.town.place.overlap", "Too close to another building", "Zu nah an einem anderen Gebäude");
        t.add("message.kingdomsatwar.town.place.uneven", "The ground is too steep here", "Das Gelände ist hier zu steil");
        t.add("message.kingdomsatwar.town.place.water", "Too much water here", "Hier ist zu viel Wasser");
        t.add("message.kingdomsatwar.town.place.tier", "Your settlement is not big enough for this yet", "Dafür ist deine Siedlung noch nicht groß genug");
        t.add("message.kingdomsatwar.town.place.not_enough", "Not enough coins", "Nicht genug Münzen");
        t.add("message.kingdomsatwar.town.place.limit", "At most three building sites at once", "Höchstens drei Baustellen gleichzeitig");
        t.add("message.kingdomsatwar.town.project_started", "Building site set up: %s – your builder is on his way",
                "Baustelle eingerichtet: %s – dein Baumeister macht sich auf den Weg");
        t.add("message.kingdomsatwar.town.project_no_builder", "Building site set up: %s – hire a builder to build it",
                "Baustelle eingerichtet: %s – heuere einen Baumeister an, damit gebaut wird");
        t.add("message.kingdomsatwar.town.project_done", "%s in %s is finished!", "%s in %s ist fertig!");
        t.add("message.kingdomsatwar.town.citizen_died", "%s, %s of %s, has died", "%s, %s von %s, ist gestorben");
        t.add("message.kingdomsatwar.citizen.sleeping", "%s is asleep", "%s schläft");
        t.add("message.kingdomsatwar.citizen.status", "%s, %s of %s – %s", "%s, %s von %s – %s");
        t.add("message.kingdomsatwar.reputation.rank_up", "Your fame grows: %s (%s %% off with your citizens)",
                "Dein Ansehen wächst: %s (%s %% Rabatt bei deinen Bürgern)");
    }

    private static void trade(Text t) {
        t.add("gui.kingdomsatwar.trade.title", "Trade", "Handel");
        t.add("gui.kingdomsatwar.trade.left", "Trades before the break: %s", "Handel bis zur Pause: %s");
        t.add("gui.kingdomsatwar.trade.pause_hint", "Then the shop closes for %s (real time).", "Dann macht der Laden %s Pause (Echtzeit).");
        t.add("gui.kingdomsatwar.trade.hours", "%s h", "%s Std.");
        t.add("gui.kingdomsatwar.trade.minutes", "%s min", "%s Min.");
        t.add("gui.kingdomsatwar.trade.discount", "Discount: %s %%", "Rabatt: %s %%");
        t.add("gui.kingdomsatwar.trade.free", "Everything is free for you, Your Majesty!", "Für dich ist alles umsonst, Majestät!");
        t.add("gui.kingdomsatwar.trade.closed", "The shop is closed", "Der Laden ist geschlossen");
        t.add("gui.kingdomsatwar.trade.opens_in", "Opens again in %s", "Öffnet wieder in %s");
        t.add("gui.kingdomsatwar.trade.missing_items", "You don't have these goods", "Diese Ware hast du nicht");
        t.add("gui.kingdomsatwar.trade.missing_coins", "Not enough coins", "Nicht genug Münzen");
        t.add("message.kingdomsatwar.trade.done", "Done", "Erledigt");
        t.add("message.kingdomsatwar.trade.too_far", "Too far away", "Zu weit weg");
        t.add("message.kingdomsatwar.trade.closed", "The shop is closed", "Der Laden ist geschlossen");
        t.add("message.kingdomsatwar.trade.missing_items", "You don't have these goods", "Diese Ware hast du nicht");
        t.add("message.kingdomsatwar.trade.missing_coins", "Not enough coins", "Nicht genug Münzen");
        t.add("message.kingdomsatwar.trade.no_space", "No room in your inventory", "Kein Platz im Inventar");
        t.add("message.kingdomsatwar.trade.nothing", "Nothing to trade", "Nichts zu handeln");
        t.add("message.kingdomsatwar.trade.no_target", "The cartographer knows of no such place", "Der Kartograf kennt keinen solchen Ort");
        t.add("message.kingdomsatwar.trade.closing", "%s closes the shop for %s minutes", "%s schließt den Laden für %s Minuten");
    }

    private static void crown(Text t) {
        t.add("gui.kingdomsatwar.town.crown.title", "The Coronation", "Die Krönung");
        t.add("gui.kingdomsatwar.town.crown.text",
                "Whoever has broken the seal, rules a city and enjoys the highest fame takes a seat on the throne of his own throne hall"
                        + " – wearing the Royal Crown – and is crowned.",
                "Wer das Siegel gebrochen hat, eine Stadt regiert und das höchste Ansehen genießt, setzt sich mit der Königskrone auf"
                        + " den Thron im eigenen Thronsaal – und wird gekrönt.");
        t.add("gui.kingdomsatwar.town.crown.0", "The seal is broken", "Das Siegel ist gebrochen");
        t.add("gui.kingdomsatwar.town.crown.1", "Your settlement is a city", "Deine Siedlung ist eine Stadt");
        t.add("gui.kingdomsatwar.town.crown.2", "Your fame is legendary", "Dein Ansehen ist legendär");
        t.add("gui.kingdomsatwar.town.crown.3", "A throne hall stands in your city", "In deiner Stadt steht ein Thronsaal");
        t.add("gui.kingdomsatwar.town.crown.4", "You wear the Royal Crown", "Du trägst die Königskrone");
        t.add("gui.kingdomsatwar.town.crown.ready", "Everything is ready: sit down on your throne!",
                "Alles ist bereit: Setz dich auf deinen Thron!");
        t.add("gui.kingdomsatwar.town.crown.done", "Crowned %s", "Gekrönt: %s");
        t.add("gui.kingdomsatwar.town.crown.address", "Title:", "Anrede:");
        t.add("message.kingdomsatwar.crown.king", "King", "König");
        t.add("message.kingdomsatwar.crown.queen", "Queen", "Königin");
        t.add("message.kingdomsatwar.crown.missing", "You cannot be crowned yet:", "Noch kannst du nicht gekrönt werden:");
        t.add("message.kingdomsatwar.crown.begins", "The bells ring – the coronation begins. Stay seated!",
                "Die Glocken läuten – die Krönung beginnt. Bleib sitzen!");
        t.add("message.kingdomsatwar.crown.aborted", "The coronation was interrupted", "Die Krönung wurde abgebrochen");
        t.add("message.kingdomsatwar.crown.already", "You already are %s of %s", "Du bist schon %s von %s");
        t.add("message.kingdomsatwar.crown.title", "Long live the King!", "Lang lebe der König!");
        t.add("message.kingdomsatwar.crown.title_queen", "Long live the Queen!", "Lang lebe die Königin!");
        t.add("message.kingdomsatwar.crown.subtitle", "%s rules over %s", "%s herrscht über %s");
        t.add("message.kingdomsatwar.crown.announce", "%s has been crowned King of %s!", "%s wurde zum König von %s gekrönt!");
        t.add("message.kingdomsatwar.crown.announce_queen", "%s has been crowned Queen of %s!", "%s wurde zur Königin von %s gekrönt!");
        t.add("message.kingdomsatwar.crown.free", "From now on your citizens give you everything for free.",
                "Von nun an geben dir deine Bürger alles umsonst.");
    }
}
