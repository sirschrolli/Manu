package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;

/** Texte des Angelns (seit 0.79): die Arten und ihre Notizen im Fischbuch, der Drill, Köder, Lebensräume. */
final class FishingLang {
    private static final String TABLE = "/data/kingdomsatwar/fishing/species_lang.tsv";

    private FishingLang() {}

    /** Die 916 Arten der Biome: Kennung, Name (englisch, deutsch), Notiz (englisch, deutsch) – je Zeile eine Art. */
    private static void biomeSpecies(HeroLang.Text text) {
        try (java.io.InputStream in = FishingLang.class.getResourceAsStream(TABLE)) {
            if (in == null) {
                throw new IllegalStateException("Die Namen der Fischarten fehlen: " + TABLE);
            }
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] f = line.split("\t", -1);
                text.add("item.kingdomsatwar.fish_" + f[0], f[1], f[2]);
                text.add("fishing.kingdomsatwar.note." + f[0], f[3], f[4]);
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    static void register(HeroLang.Text text) {
        // --- die Arten: Name (die vier Fische von Minecraft heißen, wie sie heißen) und Notiz im Fischbuch ---
        text.add("fishing.kingdomsatwar.note.cod", "The bread of the sea. Wherever a net is cast, a cod is in it.",
                "Das Brot des Meeres. Wo immer ein Netz ausgeworfen wird, liegt ein Kabeljau darin.");
        text.add("fishing.kingdomsatwar.note.salmon", "Swims upriver to where it was born – past every weir the kingdoms ever built.",
                "Zieht flussauf, dorthin, wo er geboren wurde – vorbei an jedem Wehr, das die Reiche je gebaut haben.");
        text.add("fishing.kingdomsatwar.note.tropical_fish", "As colourful as a herald's coat. Worthless in the pot, priceless in a glass bowl.",
                "Bunt wie der Rock eines Herolds. Im Topf wertlos, im Glas unbezahlbar.");
        text.add("fishing.kingdomsatwar.note.pufferfish", "Puffs itself up when afraid. Only fools and alchemists eat it.",
                "Bläht sich auf, wenn er Angst hat. Nur Narren und Alchemisten essen ihn.");
        text.add("item.kingdomsatwar.fish_brook_trout", "Brook Trout", "Bachforelle");
        text.add("fishing.kingdomsatwar.note.brook_trout", "Quick and wary, with red spots. Eat one and you run for a while as fast as it swims.",
                "Flink und wachsam, mit roten Tupfen. Wer sie isst, läuft eine Weile so schnell, wie sie schwimmt.");
        text.add("item.kingdomsatwar.fish_river_perch", "River Perch", "Flussbarsch");
        text.add("fishing.kingdomsatwar.note.river_perch", "Hunts in shoals among the reeds and only bites in daylight.",
                "Jagt im Schwarm zwischen den Schilfhalmen und beißt nur bei Tageslicht.");
        text.add("item.kingdomsatwar.fish_carp", "Carp", "Karpfen");
        text.add("fishing.kingdomsatwar.note.carp", "Unhurried and tough. In the monastery ponds of the kingdoms it lives a hundred years.",
                "Gemächlich und zäh. In den Klosterteichen der Reiche wird er hundert Jahre alt.");
        text.add("item.kingdomsatwar.fish_pike", "Pike", "Hecht");
        text.add("fishing.kingdomsatwar.note.pike", "Hangs motionless in the weeds, then strikes. On the line it does just the same.",
                "Steht reglos im Kraut und schießt dann los. An der Schnur tut er dasselbe.");
        text.add("item.kingdomsatwar.fish_catfish", "Catfish", "Wels");
        text.add("fishing.kingdomsatwar.note.catfish", "The burrower of the night. It sees in the dark with its barbels – and so do you after eating one.",
                "Der Wühler der Nacht. Mit seinen Barteln sieht er im Dunkeln – und wer ihn isst, eine Weile auch.");
        text.add("item.kingdomsatwar.fish_eel", "Eel", "Aal");
        text.add("fishing.kingdomsatwar.note.eel", "On rainy nights it even crosses wet meadows. Slippery, wriggling, hard to hold.",
                "Wandert in Regennächten sogar über nasse Wiesen. Glatt, zappelig, kaum zu halten.");
        text.add("item.kingdomsatwar.fish_sturgeon", "Sturgeon", "Stör");
        text.add("fishing.kingdomsatwar.note.sturgeon", "An armoured giant from before the dragons. It pulls stubbornly for the bottom.",
                "Ein gepanzerter Riese aus der Zeit vor den Drachen. Zieht stur zum Grund.");
        text.add("item.kingdomsatwar.fish_goldfish", "Goldfish", "Goldfisch");
        text.add("fishing.kingdomsatwar.note.goldfish", "Escaped from a castle pond, they say. Brings luck – caught or eaten.",
                "Aus einem Schlossteich entwischt, sagt man. Bringt Glück – gefangen wie gegessen.");
        text.add("item.kingdomsatwar.fish_piranha", "Piranha", "Piranha");
        text.add("fishing.kingdomsatwar.note.piranha", "Small, hungry and never alone. Makes you strong – and as hungry as itself.",
                "Klein, hungrig und nie allein. Macht stark – und so hungrig wie ihn selbst.");
        text.add("item.kingdomsatwar.fish_arapaima", "Arapaima", "Arapaima");
        text.add("fishing.kingdomsatwar.note.arapaima", "The giant of the jungle rivers, longer than a man. It must surface to breathe.",
                "Der Riese der Dschungelflüsse, länger als ein Mann. Muss zum Atmen auftauchen.");
        text.add("item.kingdomsatwar.fish_mudskipper", "Mudskipper", "Schlammspringer");
        text.add("fishing.kingdomsatwar.note.mudskipper", "A fish that would rather hop about on land. Its jump is catching.",
                "Ein Fisch, der lieber an Land hüpft. Sein Sprung steckt an.");
        text.add("item.kingdomsatwar.fish_arctic_char", "Arctic Char", "Seesaibling");
        text.add("fishing.kingdomsatwar.note.arctic_char", "Lives beneath the ice where nothing else does. Its flesh toughens you.",
                "Lebt unter dem Eis, wo sonst nichts mehr lebt. Sein Fleisch härtet ab.");
        text.add("item.kingdomsatwar.fish_rainbow_trout", "Rainbow Trout", "Regenbogenforelle");
        text.add("fishing.kingdomsatwar.note.rainbow_trout", "Rises when rain drums on the mountain brook. Its shimmer heals.",
                "Steigt, wenn der Regen auf den Bergbach trommelt. Ihr Schimmer heilt.");
        text.add("item.kingdomsatwar.fish_desert_pupfish", "Desert Pupfish", "Wüstenkärpfling");
        text.add("fishing.kingdomsatwar.note.desert_pupfish", "A finger long and tough: it endures in waterholes close to boiling.",
                "Fingerlang und zäh: überdauert in Wasserlöchern, die fast kochen.");
        text.add("item.kingdomsatwar.fish_cave_tetra", "Cave Tetra", "Höhlensalmler");
        text.add("fishing.kingdomsatwar.note.cave_tetra", "Blind and pale – it needs no eyes. Whoever eats it needs no light.",
                "Blind und bleich – er braucht keine Augen. Wer ihn isst, braucht kein Licht.");
        text.add("item.kingdomsatwar.fish_dripstone_loach", "Dripstone Loach", "Tropfsteinschmerle");
        text.add("fishing.kingdomsatwar.note.dripstone_loach", "Scrapes lime off the dripstone. Miners swear by it.",
                "Schabt Kalk von den Tropfsteinen. Bergleute schwören auf sie.");
        text.add("item.kingdomsatwar.fish_sculk_eel", "Sculk Eel", "Sculk-Aal");
        text.add("fishing.kingdomsatwar.note.sculk_eel", "Winds silently through the waters of the Deep Dark. The Warden cannot hear it – and barely sees you afterwards.",
                "Windet sich lautlos durch die Wasser des Tiefen Dunkels. Der Wärter hört ihn nicht – und dich sieht danach kaum einer.");
        text.add("item.kingdomsatwar.fish_blossom_koi", "Blossom Koi", "Blütenkoi");
        text.add("fishing.kingdomsatwar.note.blossom_koi", "Hard to spot among fallen cherry petals. A bringer of luck.",
                "Zwischen herabgefallenen Kirschblüten kaum zu sehen. Ein Glücksbringer.");
        text.add("item.kingdomsatwar.fish_gloom_pike", "Gloom Pike", "Düsterhecht");
        text.add("fishing.kingdomsatwar.note.gloom_pike", "Lurks in the pools of the dark forest, dark as the leaves above.",
                "Lauert in den Tümpeln des Düsterwalds, dunkel wie das Laub darüber.");
        text.add("item.kingdomsatwar.fish_shroom_perch", "Shroom Perch", "Pilzbarsch");
        text.add("fishing.kingdomsatwar.note.shroom_perch", "Feeds on the spores of the mushroom isles. Filling as a roast – and a little dizzying.",
                "Frisst die Sporen der Pilzinseln. Sättigt wie ein Braten – und macht ein wenig schwindlig.");
        text.add("item.kingdomsatwar.fish_herring", "Herring", "Hering");
        text.add("fishing.kingdomsatwar.note.herring", "Silver in shoals as big as clouds. Eat herring and you hold your breath like one.",
                "Silber in Schwärmen, groß wie Wolken. Wer Hering isst, hält die Luft an wie einer.");
        text.add("item.kingdomsatwar.fish_mackerel", "Mackerel", "Makrele");
        text.add("fishing.kingdomsatwar.note.mackerel", "Tiger-striped and arrow-fast – always in a hurry, on the hook too.",
                "Getigert und pfeilschnell – immer in Eile, auch am Haken.");
        text.add("item.kingdomsatwar.fish_redfish", "Redfish", "Rotbarsch");
        text.add("fishing.kingdomsatwar.note.redfish", "Bright red from cold water. A catch that warms.",
                "Leuchtend rot aus kaltem Wasser. Ein Fang, der wärmt.");
        text.add("item.kingdomsatwar.fish_plaice", "Plaice", "Scholle");
        text.add("fishing.kingdomsatwar.note.plaice", "Lies flat in the sand, both eyes on one side. It pulls down, always down.",
                "Liegt platt im Sand, beide Augen auf einer Seite. Zieht nach unten, immer nach unten.");
        text.add("item.kingdomsatwar.fish_tuna", "Tuna", "Thunfisch");
        text.add("fishing.kingdomsatwar.note.tuna", "A muscle with fins. The sailors of the kingdoms call it the steed of the sea.",
                "Ein Muskel mit Flossen. Die Seeleute der Reiche nennen ihn das Ross des Meeres.");
        text.add("item.kingdomsatwar.fish_swordfish", "Swordfish", "Schwertfisch");
        text.add("fishing.kingdomsatwar.note.swordfish", "Its sword has pierced boat planks before. A duel, not a catch.",
                "Sein Schwert hat schon Bootsplanken durchbohrt. Ein Duell, kein Fang.");
        text.add("item.kingdomsatwar.fish_sunfish", "Ocean Sunfish", "Mondfisch");
        text.add("fishing.kingdomsatwar.note.sunfish", "Drifts like a disc beneath the surface, basking in the sun.",
                "Treibt wie eine Scheibe unter der Oberfläche und lässt sich von der Sonne wärmen.");
        text.add("item.kingdomsatwar.fish_seahorse", "Seahorse", "Seepferdchen");
        text.add("fishing.kingdomsatwar.note.seahorse", "Clings to coral with its tail. It swims as the dolphins do – and so will you.",
                "Hält sich mit dem Schwanz an Korallen fest. Schwimmt, wie die Delfine schwimmen – und du danach auch.");
        text.add("item.kingdomsatwar.fish_parrotfish", "Parrotfish", "Papageifisch");
        text.add("fishing.kingdomsatwar.note.parrotfish", "Nibbles coral and turns it to sand. Colourful, lively, catching.",
                "Knabbert Korallen und macht daraus Sand. Bunt, munter, ansteckend.");
        text.add("item.kingdomsatwar.fish_lionfish", "Lionfish", "Rotfeuerfisch");
        text.add("fishing.kingdomsatwar.note.lionfish", "Every spine a poisoned dart. The poison passes – the strength lingers a little longer.",
                "Jeder Stachel ein Giftpfeil. Das Gift vergeht – die Kraft bleibt etwas länger.");
        text.add("item.kingdomsatwar.fish_octopus", "Octopus", "Oktopus");
        text.add("fishing.kingdomsatwar.note.octopus", "Eight arms, three hearts, no bones. Gone if you so much as blink.",
                "Acht Arme, drei Herzen, keine Knochen. Verschwindet, wenn man nur blinzelt.");
        text.add("item.kingdomsatwar.fish_catshark", "Catshark", "Katzenhai");
        text.add("fishing.kingdomsatwar.note.catshark", "A small shark that roams the seabed at night. Its eyes shine in the dark.",
                "Ein kleiner Hai, der nachts über den Grund streift. Seine Augen leuchten im Dunkeln.");
        text.add("item.kingdomsatwar.fish_halibut", "Halibut", "Heilbutt");
        text.add("fishing.kingdomsatwar.note.halibut", "The barn door among flatfish – it grows that large. From the cold north.",
                "Das Scheunentor unter den Plattfischen – so groß kann er werden. Aus dem kalten Norden.");
        text.add("item.kingdomsatwar.fish_lanternfish", "Lanternfish", "Laternenfisch");
        text.add("fishing.kingdomsatwar.note.lanternfish", "A spark in the deep. In shoals they look like a starry sky under water.",
                "Ein Funke in der Tiefe. In Schwärmen sehen sie aus wie ein Sternenhimmel unter Wasser.");
        text.add("item.kingdomsatwar.fish_hatchetfish", "Hatchetfish", "Beilfisch");
        text.add("fishing.kingdomsatwar.note.hatchetfish", "Thin as a blade, light as a feather.",
                "Dünn wie eine Klinge, leicht wie eine Feder.");
        text.add("item.kingdomsatwar.fish_anglerfish", "Anglerfish", "Anglerfisch");
        text.add("fishing.kingdomsatwar.note.anglerfish", "It angles too – with a light before its jaws. Whoever catches it had the better bait.",
                "Angelt selbst – mit einem Licht vor dem Maul. Wer ihn fängt, hatte den besseren Köder.");
        text.add("item.kingdomsatwar.fish_viperfish", "Viperfish", "Viperfisch");
        text.add("fishing.kingdomsatwar.note.viperfish", "Teeth too long for its own mouth. Its strength consumes – you as well.",
                "Zähne, zu lang für das eigene Maul. Seine Kraft zehrt – auch an dir.");
        text.add("item.kingdomsatwar.fish_firefly_squid", "Firefly Squid", "Leuchtkalmar");
        text.add("fishing.kingdomsatwar.note.firefly_squid", "Flashes blue when frightened – and it is frightened often.",
                "Blinkt blau, wenn er sich fürchtet – und er fürchtet sich oft.");
        text.add("item.kingdomsatwar.fish_giant_isopod", "Giant Isopod", "Riesenassel");
        text.add("fishing.kingdomsatwar.note.giant_isopod", "A woodlouse the size of a helmet. Armoured, and slow to match.",
                "Eine Kellerassel, so groß wie ein Helm. Gepanzert und entsprechend langsam.");
        text.add("item.kingdomsatwar.fish_oarfish", "Oarfish", "Riemenfisch");
        text.add("fishing.kingdomsatwar.note.oarfish", "The sea serpent of the sailors' songs: a ribbon of silver longer than a boat.",
                "Die Seeschlange der Seemannslieder: ein Band aus Silber, länger als ein Boot.");
        text.add("item.kingdomsatwar.fish_ghost_shark", "Ghost Shark", "Geisterhai");
        text.add("fishing.kingdomsatwar.note.ghost_shark", "Pale, silent, with eyes like emeralds. Eat it and you become a wraith yourself.",
                "Bleich, lautlos, mit Augen wie Smaragde. Wer ihn isst, wird selbst zum Schemen.");
        text.add("item.kingdomsatwar.fish_coelacanth", "Coelacanth", "Quastenflosser");
        text.add("fishing.kingdomsatwar.note.coelacanth", "Thought extinct since the dragons were young. It simply kept swimming.",
                "Galt als ausgestorben, seit die Drachen jung waren. Er ist einfach weitergeschwommen.");
        text.add("item.kingdomsatwar.fish_vampire_squid", "Vampire Squid", "Vampirkalmar");
        text.add("fishing.kingdomsatwar.note.vampire_squid", "Wraps itself in its cloak like a count. Its blood heals, its shadow lingers.",
                "Hüllt sich in seinen Mantel wie ein Graf. Sein Blut heilt, sein Schatten bleibt.");
        text.add("item.kingdomsatwar.fish_magma_carp", "Magma Carp", "Magmakarpfen");
        text.add("fishing.kingdomsatwar.note.magma_carp", "Scales like cooling embers. It swims through lava as others do through a pond.",
                "Schuppen wie erkaltende Glut. Schwimmt durch Lava wie andere durch einen Teich.");
        text.add("item.kingdomsatwar.fish_crimson_snapper", "Crimson Snapper", "Karmesinschnapper");
        text.add("fishing.kingdomsatwar.note.crimson_snapper", "From the lava lakes beneath the crimson forest. Even the hoglins shun the shore.",
                "Aus den Lavaseen unter dem Karmesinwald. Selbst die Hoglins meiden das Ufer.");
        text.add("item.kingdomsatwar.fish_warp_fin", "Warp Fin", "Wirrflosse");
        text.add("fishing.kingdomsatwar.note.warp_fin", "It is never quite where you see it. Neither are you, once you have eaten it.",
                "Ist nie ganz dort, wo man sie sieht. Wer sie isst, auch nicht.");
        text.add("item.kingdomsatwar.fish_soul_eel", "Soul Eel", "Seelenaal");
        text.add("fishing.kingdomsatwar.note.soul_eel", "It whispers when lifted from the lava. Grants the haste of the restless.",
                "Flüstert, wenn man ihn aus der Lava hebt. Verleiht die Eile der Rastlosen.");
        text.add("item.kingdomsatwar.fish_basalt_pike", "Basalt Pike", "Basalthecht");
        text.add("fishing.kingdomsatwar.note.basalt_pike", "As hard as the columns it hunts between.",
                "Hart wie die Säulen, zwischen denen er jagt.");
        text.add("item.kingdomsatwar.fish_ember_ray", "Ember Ray", "Glutrochen");
        text.add("fishing.kingdomsatwar.note.ember_ray", "Glides over lava like a leaf on the wind. Its flesh does not burn – and neither will you.",
                "Gleitet über die Lava wie ein Blatt im Wind. Sein Fleisch brennt nicht – und du danach auch nicht.");
        text.add("item.kingdomsatwar.fish_ender_salmon", "Ender Salmon", "Endlachs");
        text.add("fishing.kingdomsatwar.note.ender_salmon", "Leaps through the void between the islands. Where to, not even it knows.",
                "Springt zwischen den Inseln durch die Leere. Wohin, weiß er selbst nicht.");
        text.add("item.kingdomsatwar.fish_chorus_fish", "Chorus Fish", "Chorusfisch");
        text.add("fishing.kingdomsatwar.note.chorus_fish", "Feeds on fallen chorus blossoms and floats off like them.",
                "Frisst herabgefallene Chorusblüten und schwebt davon wie sie.");
        text.add("item.kingdomsatwar.fish_void_ray", "Void Ray", "Leerenrochen");
        text.add("fishing.kingdomsatwar.note.void_ray", "A shadow with stars on its back, circling silently beneath the islands.",
                "Ein Schatten mit Sternen auf dem Rücken, der lautlos unter den Inseln kreist.");
        text.add("item.kingdomsatwar.fish_stardust_minnow", "Stardust Minnow", "Sternstaub-Elritze");
        text.add("fishing.kingdomsatwar.note.stardust_minnow", "Tiny and glittering. The Void is full of them – they are merely mistaken for stars.",
                "Winzig und funkelnd. Die Leere ist voll davon – man hält sie nur für Sterne.");
        text.add("item.kingdomsatwar.fish_crystal_fin", "Crystal Fin", "Kristallflosse");
        text.add("fishing.kingdomsatwar.note.crystal_fin", "Half fish, half splinter of void crystal. Hard as glass that will not break.",
                "Halb Fisch, halb Splitter eines Leerenkristalls. Hart wie Glas, das nicht bricht.");
        text.add("item.kingdomsatwar.fish_glimmerfish", "Glimmerfish", "Glimmerfisch");
        text.add("fishing.kingdomsatwar.note.glimmerfish", "It glows when someone laughs – so the fae say. Gives away anyone who has eaten it.",
                "Leuchtet, wenn jemand lacht – sagen die Feen. Verrät jeden, der ihn gegessen hat.");
        text.add("item.kingdomsatwar.fish_mushroom_swimmer", "Mushroom Swimmer", "Pilzschwimmer");
        text.add("fishing.kingdomsatwar.note.mushroom_swimmer", "Lives beneath the caps of the giant mushrooms. It hops in the water; eat it and you hop on land.",
                "Lebt unter den Hüten der Riesenpilze. Hüpft im Wasser – und wer ihn isst, an Land.");
        text.add("item.kingdomsatwar.fish_moonlily_koi", "Moonlily Koi", "Mondlilien-Koi");
        text.add("fishing.kingdomsatwar.note.moonlily_koi", "Sleeps beneath the moon lilies by day and draws silver circles by night.",
                "Schläft tags unter den Mondlilien und zieht nachts silberne Kreise.");
        text.add("item.kingdomsatwar.fish_thorn_pike", "Thorn Pike", "Dornenhecht");
        text.add("fishing.kingdomsatwar.note.thorn_pike", "A hunter with spines like a rose briar. Prickly to the bone.",
                "Ein Jäger mit Stacheln wie eine Rosenranke. Wehrhaft bis in die Gräten.");
        text.add("item.kingdomsatwar.fish_mist_carp", "Mist Carp", "Nebelkarpfen");
        text.add("fishing.kingdomsatwar.note.mist_carp", "Grey as the mist over the moors of the Shadow World – and almost as hard to grasp.",
                "Grau wie der Nebel über den Mooren der Schattenwelt – und fast so schwer zu fassen.");
        text.add("item.kingdomsatwar.fish_blind_pike", "Blind Pike", "Blindhecht");
        text.add("fishing.kingdomsatwar.note.blind_pike", "It has unlearned light and hunts by heartbeat. Eat it and you briefly see what it sees: nothing.",
                "Hat das Licht verlernt und jagt nach dem Herzschlag. Wer ihn isst, sieht kurz, was er sieht: nichts.");
        text.add("item.kingdomsatwar.fish_shadow_eel", "Shadow Eel", "Schattenaal");
        text.add("fishing.kingdomsatwar.note.shadow_eel", "The Umbrer smoke it over soul fire. Whoever eats of it walks unseen.",
                "Die Umbrer räuchern ihn über Seelenfeuer. Wer davon isst, geht ungesehen.");
        text.add("item.kingdomsatwar.fish_soulfish", "Soulfish", "Seelenfisch");
        text.add("fishing.kingdomsatwar.note.soulfish", "Translucent and still. They say it carries off what torments you.",
                "Durchscheinend und still. Es heißt, er trage fort, was einen quält.");
        text.add("item.kingdomsatwar.fish_ember_catfish", "Ember Catfish", "Glutwels");
        text.add("fishing.kingdomsatwar.note.ember_catfish", "Burrows in the hot mud of the Abyss. Its barbels smoulder.",
                "Wühlt im heißen Schlamm des Abgrunds. Seine Barteln glimmen.");
        text.add("item.kingdomsatwar.fish_chaos_fin", "Chaos Fin", "Chaosflosse");
        text.add("fishing.kingdomsatwar.note.chaos_fin", "No two are alike, and none stays where it is.",
                "Keine gleicht der anderen, und keine bleibt, wo sie ist.");
        text.add("item.kingdomsatwar.fish_rift_eel", "Rift Eel", "Rissaal");
        text.add("fishing.kingdomsatwar.note.rift_eel", "Slithers through the rifts of the world. It lifts you up – and lets you down gently.",
                "Schlängelt sich durch die Risse der Welt. Hebt dich hoch – und lässt dich sanft wieder herab.");
        text.add("item.kingdomsatwar.fish_old_one", "The Old One of the Lake", "Der Alte vom See");
        text.add("fishing.kingdomsatwar.note.old_one", "A carp older than the Seal. Every fisher knows of it; none has ever held it. It bites only at night.",
                "Ein Karpfen, älter als das Siegel. Jeder Fischer kennt ihn, keiner hat ihn je gehalten. Er beißt nur nachts.");
        text.add("item.kingdomsatwar.fish_goldscale", "Goldscale", "Goldschuppe");
        text.add("fishing.kingdomsatwar.note.goldscale", "Shows itself only when the sun breaks through the rain. One scale outweighs a kingdom, they say.",
                "Zeigt sich nur, wenn die Sonne durch den Regen bricht. Eine Schuppe wiegt ein Königreich auf, heißt es.");
        text.add("item.kingdomsatwar.fish_stormsail", "Stormsail", "Sturmsegler");
        text.add("fishing.kingdomsatwar.note.stormsail", "Rides the waves of the thunderstorm and no others. Its sail catches lightning.",
                "Reitet die Wellen des Gewitters und keine anderen. Sein Segel fängt den Blitz.");
        text.add("item.kingdomsatwar.fish_deep_king", "King of the Deep", "König der Tiefe");
        text.add("fishing.kingdomsatwar.note.deep_king", "The oldest angler of the deep sea. Its light has lured ships off course.",
                "Der älteste Angler der Tiefsee. Sein Licht hat schon Schiffe vom Kurs gelockt.");
        text.add("item.kingdomsatwar.fish_inferno_king", "Inferno King", "Infernokönig");
        text.add("fishing.kingdomsatwar.note.inferno_king", "Armoured in black stone, embers in its veins. It has ruled the lava seas since they first flowed.",
                "Gepanzert in schwarzen Stein, in den Adern Glut. Er herrscht über die Lavaseen, seit es sie gibt.");
        text.add("item.kingdomsatwar.fish_dragon_koi", "Dragon Koi", "Drachenkoi");
        text.add("fishing.kingdomsatwar.note.dragon_koi", "A koi that once swam through the End gate and never found its way back. Now half fish, half dragon.",
                "Ein Koi, der einst durch das Endtor schwamm und nicht zurückfand. Heute halb Fisch, halb Drache.");
        text.add("item.kingdomsatwar.fish_moonshine_koi", "Moonshine Koi", "Mondschein-Koi");
        text.add("fishing.kingdomsatwar.note.moonshine_koi", "Made of moonlight, the fae say. Whoever eats it grows a second heart for a while.",
                "Aus Mondlicht gemacht, sagen die Feen. Wer ihn isst, dem wächst für eine Weile ein zweites Herz.");
        // --- die Legenden seit 0.81 ---
        text.add("item.kingdomsatwar.fish_night_sovereign", "Night Sovereign", "Nachtfürst");
        text.add("fishing.kingdomsatwar.note.night_sovereign",
                "The master of the black waters of the Shadow World. Older than the Seal, they say, and no friend of light.",
                "Der Herr der schwarzen Wasser der Schattenwelt. Älter als das Siegel, sagt man, und kein Freund des Lichts.");
        text.add("item.kingdomsatwar.fish_rift_leviathan", "Rift Leviathan", "Rissleviathan");
        text.add("fishing.kingdomsatwar.note.rift_leviathan",
                "Swims where the worlds were torn apart – through water, embers and the nothing between. Its back glows like the Seam itself.",
                "Schwimmt, wo die Welten zerrissen wurden – durch Wasser, Glut und das Nichts dazwischen. Sein Rücken glüht wie die Naht selbst.");
        text.add("item.kingdomsatwar.fish_star_whale", "Star Whale", "Sternenwal");
        text.add("fishing.kingdomsatwar.note.star_whale",
                "Drifts through the Void with a whole night sky on its back. Small for a whale – vast for a fishing line.",
                "Treibt durch die Leere, einen ganzen Nachthimmel auf dem Rücken. Klein für einen Wal – gewaltig für eine Angelschnur.");
        text.add("item.kingdomsatwar.fish_lightless_one", "The Lightless One", "Der Lichtlose");
        text.add("fishing.kingdomsatwar.note.lightless_one",
                "A pale giant that has never seen the sun. It lives wherever water stands deep below the ground – and lends its senses to whoever eats it.",
                "Ein bleicher Riese, der die Sonne nie gesehen hat. Lebt überall, wo tief unter der Erde Wasser steht – und leiht seine Sinne dem, der ihn isst.");
        text.add("item.kingdomsatwar.fish_frost_emperor", "Frost Emperor", "Eiskaiser");
        text.add("fishing.kingdomsatwar.note.frost_emperor",
                "Rules beneath the ice of cold lakes and seas. Its armour of frost passes to whoever lands it.",
                "Herrscht unter dem Eis der kalten Seen und Meere. Sein Panzer aus Frost geht auf den über, der ihn landet.");
        text.add("item.kingdomsatwar.fish_reef_queen", "Reef Queen", "Riffkönigin");
        text.add("fishing.kingdomsatwar.note.reef_queen",
                "Every reef has a hundred colours – she wears them all. She shows herself only by day, above the corals of warm seas.",
                "Jedes Riff hat hundert Farben – sie trägt alle. Zeigt sich nur am Tag, über den Korallen der warmen Meere.");
        text.add("item.kingdomsatwar.fish_bog_mother", "Bog Mother", "Moormutter");
        text.add("fishing.kingdomsatwar.note.bog_mother",
                "As old as the swamp itself. The witches leave her offerings; at night she comes to fetch them.",
                "So alt wie der Sumpf selbst. Die Hexen bringen ihr Gaben; nachts kommt sie, sie zu holen.");
        text.add("item.kingdomsatwar.fish_sun_king", "Sun King", "Sonnenkönig");
        text.add("fishing.kingdomsatwar.note.sun_king",
                "Gold in a waterhole: it lives where water is rarest, and bites when the sun stands highest.",
                "Gold im Wasserloch: lebt, wo Wasser am seltensten ist, und beißt, wenn die Sonne am höchsten steht.");
        // --- die Arten der Biome (0.81): Namen und Notizen aus der Tabelle von tools/fish_gen.py ---
        biomeSpecies(text);
        // --- Köder, Filet, Drill, Fischbuch ---
        text.add("item.kingdomsatwar.bait_worm", "Bait Worm", "Köderwurm");
        text.add(ModItems.loreKey("bait_worm", 1), "In your other hand while fishing: bites come sooner",
                "Beim Angeln in der anderen Hand: Fische beißen schneller");
        text.add(ModItems.loreKey("bait_worm", 2), "Lures the common fish of rivers and lakes (one per bite)",
                "Lockt die häufigen Fische der Flüsse und Seen (je Biss einer)");
        text.add("item.kingdomsatwar.bait_glow", "Glow Bait", "Glühköder");
        text.add(ModItems.loreKey("bait_glow", 1), "In your other hand while fishing: bites come sooner",
                "Beim Angeln in der anderen Hand: Fische beißen schneller");
        text.add(ModItems.loreKey("bait_glow", 2), "Lures what lives in the dark: night fish, deep sea, caves, lava",
                "Lockt, was im Dunkeln lebt: Nachtfische, Tiefsee, Höhlen, Lava");
        text.add("item.kingdomsatwar.fish_fillet_cooked", "Cooked Fish Fillet", "Gebratenes Fischfilet");
        text.add("itemGroup.kingdomsatwar.fishing", "Kingdoms at War: Fishing", "Kingdoms at War: Angeln");
        text.add("key.kingdomsatwar.fish_book", "Open the fish book", "Fischbuch aufschlagen");
        text.add("gui.kingdomsatwar.fishing.title", "Fish on the line", "Fisch an der Leine");
        text.add("gui.kingdomsatwar.fishing.hint", "Hold or click the mouse: the zone rises. Keep the fish inside!",
                "Maus halten oder klicken hebt die Zone – halte den Fisch darin!");
        text.add("gui.kingdomsatwar.fishing.perfect_so_far", "Flawless so far – keep it up!", "Bisher makellos – weiter so!");
        text.add("gui.kingdomsatwar.fishing.bite", "A bite!", "Biss!");
        text.add("gui.kingdomsatwar.fishing.escaped", "It got away …", "Entkommen …");
        text.add("gui.kingdomsatwar.fishing.caught", "Caught!", "Gefangen!");
        text.add("gui.kingdomsatwar.fishing.perfect", "Perfect catch!", "Perfekter Fang!");
        text.add("gui.kingdomsatwar.fishing.new_species", "New species!", "Neue Art!");
        text.add("gui.kingdomsatwar.fishing.new_record", "New record!", "Neuer Rekord!");
        text.add("gui.kingdomsatwar.fishing.size", "%1$s · %2$s cm", "%1$s · %2$s cm");
        text.add("gui.kingdomsatwar.fishing.with_chest", " + treasure", " + Schatz");
        text.add("gui.kingdomsatwar.fish_book.title", "Fish Book", "Fischbuch");
        text.add("fishing.kingdomsatwar.rarity.common", "Common", "Gewöhnlich");
        text.add("fishing.kingdomsatwar.rarity.uncommon", "Uncommon", "Ungewöhnlich");
        text.add("fishing.kingdomsatwar.rarity.rare", "Rare", "Selten");
        text.add("fishing.kingdomsatwar.rarity.epic", "Epic", "Episch");
        text.add("fishing.kingdomsatwar.rarity.legendary", "Legendary", "Legendär");
        text.add("fishing.kingdomsatwar.chapter.rivers", "Rivers & Lakes", "Flüsse & Seen");
        text.add("fishing.kingdomsatwar.chapter.sea", "The Sea", "Das Meer");
        text.add("fishing.kingdomsatwar.chapter.deep", "The Deep Sea", "Die Tiefsee");
        text.add("fishing.kingdomsatwar.chapter.caves", "Caves", "Höhlen");
        text.add("fishing.kingdomsatwar.chapter.nether", "The Nether", "Der Nether");
        text.add("fishing.kingdomsatwar.chapter.end", "End & Void", "End & Leere");
        text.add("fishing.kingdomsatwar.chapter.fae", "The Fae Realm", "Das Feenreich");
        text.add("fishing.kingdomsatwar.chapter.shadow", "The Shadow World", "Die Schattenwelt");
        text.add("fishing.kingdomsatwar.chapter.abyss", "The Abyss", "Der Abgrund");
        text.add("fishing.kingdomsatwar.chapter.legends", "Legends", "Legenden");
        text.add("fishing.kingdomsatwar.habitat.fresh", "rivers and lakes", "Flüsse und Seen");
        text.add("fishing.kingdomsatwar.habitat.fresh_cold", "cold waters", "kalte Gewässer");
        text.add("fishing.kingdomsatwar.habitat.fresh_warm", "jungle waters", "Dschungelgewässer");
        text.add("fishing.kingdomsatwar.habitat.swamp", "swamps", "Sümpfe");
        text.add("fishing.kingdomsatwar.habitat.mountain", "mountain brooks and lakes", "Bergbäche und Bergseen");
        text.add("fishing.kingdomsatwar.habitat.dry", "waterholes of desert, savanna and badlands", "Wasserlöcher in Wüste, Savanne und Tafelbergen");
        text.add("fishing.kingdomsatwar.habitat.cave", "cave waters", "Höhlengewässer");
        text.add("fishing.kingdomsatwar.habitat.sculk", "the Deep Dark", "das Tiefe Dunkel");
        text.add("fishing.kingdomsatwar.habitat.blossom", "cherry groves and flower meadows", "Kirschberghaine und Blumenwiesen");
        text.add("fishing.kingdomsatwar.habitat.dark", "dark forest and pale garden", "Dunkler Wald und Blasser Garten");
        text.add("fishing.kingdomsatwar.habitat.mushroom", "mushroom fields", "Pilzland");
        text.add("fishing.kingdomsatwar.habitat.salt", "sea and shore", "Meer und Strand");
        text.add("fishing.kingdomsatwar.habitat.salt_warm", "warm seas", "warme Meere");
        text.add("fishing.kingdomsatwar.habitat.salt_cold", "cold seas", "kalte Meere");
        text.add("fishing.kingdomsatwar.habitat.deep", "the deep sea", "die Tiefsee");
        text.add("fishing.kingdomsatwar.habitat.lava", "lava, anywhere", "Lava, überall");
        text.add("fishing.kingdomsatwar.habitat.lava_crimson", "lava in the crimson forest", "Lava im Karmesinwald");
        text.add("fishing.kingdomsatwar.habitat.lava_warped", "lava in the warped forest", "Lava im Wirrwald");
        text.add("fishing.kingdomsatwar.habitat.lava_soul", "lava in the soul sand valley", "Lava im Seelensandtal");
        text.add("fishing.kingdomsatwar.habitat.lava_basalt", "lava in the basalt deltas", "Lava in den Basaltdeltas");
        text.add("fishing.kingdomsatwar.habitat.end", "the End", "das End");
        text.add("fishing.kingdomsatwar.habitat.void", "the Void", "die Leere");
        text.add("fishing.kingdomsatwar.chapter.rivers.tip",
                "Rivers, lakes and ponds of the Overworld. Some fish bite everywhere – every biome has its own besides.",
                "Flüsse, Seen und Teiche der Oberwelt. Manche Fische beißen überall – dazu hat jedes Biom seine eigenen.");
        text.add("fishing.kingdomsatwar.chapter.sea.tip", "The seas and their shores. Every ocean, every beach and both coasts have their own species.",
                "Die Meere und ihre Küsten. Jeder Ozean, jeder Strand und beide Küsten haben eigene Arten.");
        text.add("fishing.kingdomsatwar.chapter.deep.tip", "Above the deep oceans. More bites at night – and with glow bait in your other hand.",
                "Über den tiefen Ozeanen. Nachts beißt mehr – und mit Glühköder in der anderen Hand.");
        text.add("fishing.kingdomsatwar.chapter.caves.tip",
                "Water deep below the ground: in any cave, and with species of their own in the cave biomes. Glow bait helps.",
                "Wasser tief unter der Erde: in jeder Höhle, und mit eigenen Arten in den Höhlenbiomen. Glühköder hilft.");
        text.add("fishing.kingdomsatwar.chapter.nether.tip",
                "The bobber floats on lava, too. Every biome of the Nether has its own fish – and they do not burn.",
                "Der Schwimmer treibt auch auf Lava. Jedes Biom des Nethers hat eigene Fische – und die verbrennen nicht.");
        text.add("fishing.kingdomsatwar.chapter.end.tip",
                "Cast the hook over the edge of an island: a few blocks down, the void carries it like water. The End and the Void, biome by biome.",
                "Wirf den Haken über den Rand einer Insel: Ein paar Blöcke tiefer trägt ihn die Leere wie Wasser. End und Leere, Biom für Biom.");
        text.add("fishing.kingdomsatwar.chapter.fae.tip", "In the waters of the Fae Realm, beyond the fae portal – each of its lands has its own.",
                "In den Wassern des Feenreichs, jenseits des Feenportals – jede seiner Landschaften hat eigene Arten.");
        text.add("fishing.kingdomsatwar.chapter.shadow.tip", "In the still, dark waters of the Shadow World – from the Shadow Forest down to the Shadow Depths.",
                "In den stillen, dunklen Wassern der Schattenwelt – vom Schattenwald bis hinab in die Schattentiefen.");
        text.add("fishing.kingdomsatwar.chapter.abyss.tip", "In the Abyss – in its water, its lava, and the void between its islands.",
                "Im Abgrund – in seinem Wasser, seiner Lava und der Leere zwischen seinen Inseln.");
        text.add("fishing.kingdomsatwar.chapter.legends.tip",
                "Fifteen in all the worlds. Very rare, hardly to be held – Luck of the Sea draws them and widens the catch zone.",
                "Fünfzehn in allen Welten. Sehr selten, kaum zu halten – Glück des Meeres lockt sie und vergrößert die Fangzone.");
        text.add("fishing.kingdomsatwar.book.everywhere", "Everywhere", "Überall");
        text.add("fishing.kingdomsatwar.book.all_worlds", "In all the worlds", "In allen Welten");
        text.add("fishing.kingdomsatwar.medium.lava", " (in lava)", " (in Lava)");
        text.add("fishing.kingdomsatwar.medium.void", " (in the void)", " (in der Leere)");
        // --- der Fischbeutel (0.81) ---
        text.add("item.kingdomsatwar.fish_bag", "Fish Bag", "Fischbeutel");
        text.add("item.kingdomsatwar.fish_bag.level", "Level %s", "Stufe %s");
        text.add("item.kingdomsatwar.fish_bag.fill", "%1$s / %2$s fish", "%1$s / %2$s Fische");
        text.add("item.kingdomsatwar.fish_bag.more", " … and %s more species", " … und %s weitere Arten");
        text.add("item.kingdomsatwar.fish_bag.hint.catch", "Carried along: every catch goes inside", "Dabei getragen: Jeder Fang wandert hinein");
        text.add("item.kingdomsatwar.fish_bag.hint.open", "Right-click: open and take fish out", "Rechtsklick: öffnen und Fische entnehmen");
        text.add("item.kingdomsatwar.fish_bag.hint.upgrade", "The fisher in the village sells more room", "Mehr Platz verkauft der Fischer im Dorf");
        text.add("item.kingdomsatwar.fish_bag.upgrade_hint", "For the bag you carry – its contents stay inside",
                "Für den Beutel, den du bei dir trägst – der Inhalt bleibt darin");
        text.add("trade.kingdomsatwar.fish_bag_upgrade", "Bag extension: level %1$s (%2$s fish)", "Beutel-Erweiterung: Stufe %1$s (%2$s Fische)");
        text.add("trade.kingdomsatwar.fish.common", "Common fish", "Gewöhnliche Fische");
        text.add("trade.kingdomsatwar.fish.uncommon", "Uncommon fish", "Ungewöhnliche Fische");
        text.add("trade.kingdomsatwar.fish.rare", "Rare fish", "Seltene Fische");
        text.add("trade.kingdomsatwar.fish.epic", "Epic fish", "Epische Fische");
        text.add("trade.kingdomsatwar.fish.any", "Any species of this rarity", "Beliebige Arten dieser Seltenheit");
        text.add("trade.kingdomsatwar.fish.from", "From your inventory or your fish bag", "Aus dem Inventar oder dem Fischbeutel");
        text.add("gui.kingdomsatwar.trade.missing_bag", "You carry no fish bag", "Du trägst keinen Fischbeutel bei dir");
        text.add("message.kingdomsatwar.fish_bag.inventory_full", "No room in your inventory", "Kein Platz im Inventar");
        text.add("message.kingdomsatwar.fish_bag.full", "Your fish bag is full – the fisher in the village sells more room",
                "Dein Fischbeutel ist voll – mehr Platz verkauft der Fischer im Dorf");
        text.add("item.kingdomsatwar.fish.home", "Home: %s", "Heimat: %s");
        text.add("gui.kingdomsatwar.fishing.into_bag", " → bag", " → Beutel");
        text.add("gui.kingdomsatwar.fish_bag.title", "Fish Bag", "Fischbeutel");
        text.add("gui.kingdomsatwar.fish_bag.level", "Level %s", "Stufe %s");
        text.add("gui.kingdomsatwar.fish_bag.fill", "%1$s / %2$s", "%1$s / %2$s");
        text.add("gui.kingdomsatwar.fish_bag.empty", "The bag is empty. Whatever you catch goes in here.",
                "Der Beutel ist leer. Was du angelst, wandert hinein.");
        text.add("gui.kingdomsatwar.fish_bag.take_all", "Take all", "Alles entnehmen");
        text.add("gui.kingdomsatwar.fish_bag.book", "Fish book", "Fischbuch");
        text.add("gui.kingdomsatwar.fish_bag.hint", "Click: a stack · Shift-click: all · Right-click: one",
                "Klick: ein Stapel · Umschalt-Klick: alle · Rechtsklick: einer");
        text.add("gui.kingdomsatwar.fish_bag.next", "Room for %s at the fisher's:", "Platz für %s beim Fischer:");
        text.add("fishing.kingdomsatwar.habitat.fae", "the Fae Realm", "das Feenreich");
        text.add("fishing.kingdomsatwar.habitat.shadow", "the Shadow World", "die Schattenwelt");
        text.add("fishing.kingdomsatwar.habitat.abyss", "the Abyss", "der Abgrund");
        text.add("fishing.kingdomsatwar.time.any", "Day and night", "Tag und Nacht");
        text.add("fishing.kingdomsatwar.time.day", "By day (rarely at night)", "Tagsüber (nachts selten)");
        text.add("fishing.kingdomsatwar.time.night", "At night (rarely by day)", "Nachts (tagsüber selten)");
        text.add("fishing.kingdomsatwar.weather.rain", "Bites best in the rain", "Beißt am liebsten bei Regen");
        text.add("fishing.kingdomsatwar.weather.rain_only", "Only in the rain", "Nur bei Regen");
        text.add("fishing.kingdomsatwar.weather.thunder_only", "Only in a thunderstorm", "Nur im Gewitter");
        text.add("fishing.kingdomsatwar.size", "%1$s–%2$s cm", "%1$s–%2$s cm");
        text.add("fishing.kingdomsatwar.record", "Record: %s cm", "Rekord: %s cm");
        text.add("fishing.kingdomsatwar.count", "Caught: %s", "Gefangen: %s");
        text.add("fishing.kingdomsatwar.count.none", "Not caught yet", "Noch nicht gefangen");
        text.add("fishing.kingdomsatwar.eaten", "When eaten:", "Gegessen:");
        text.add("fishing.kingdomsatwar.eaten.none", "Tastes the way it always has", "Schmeckt, wie er immer geschmeckt hat");
        text.add("fishing.kingdomsatwar.eaten.unknown", "Effect unknown", "Wirkung unbekannt");
        text.add("fishing.kingdomsatwar.uncaught", "The book only knows more once one has been on your hook.",
                "Mehr weiß das Buch erst, wenn einer am Haken hing.");
        text.add("fishing.kingdomsatwar.special.teleport_far", "Carries you off like a chorus fruit", "Versetzt dich wie eine Chorusfrucht");
        text.add("fishing.kingdomsatwar.special.teleport_near", "Shifts you a few steps", "Versetzt dich ein paar Schritte");
        text.add("fishing.kingdomsatwar.special.cleanse", "Lifts all ailments", "Nimmt alle Leiden");
        text.add("fishing.kingdomsatwar.book.treasures", "%1$s fish caught · %2$s treasures", "%1$s Fische gefangen · %2$s Schätze");
    }
}
