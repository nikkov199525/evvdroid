# evvdroid

Eloquence as a text-to-speech engine for Android and Wear OS.

The speech comes from [openevv](https://github.com/Mudb0y/openevv), a C rebuild of IBM's Embedded ViaVoice. This repo cross-compiles that engine for Android, wraps it in JNI, and puts a `TextToSpeechService` around it. openevv is a submodule and is not edited in place.

## Unofficial

A hobby project. Nobody's product. Not from IBM, Nuance, Cerence, Google or the openevv project, and none of them have endorsed or reviewed it.

It is not a supported screen reader engine. If you set it as your TalkBack voice and it stops talking, you have to undo that yourself, so read "Turning it off" before you turn it on.

No warranty. See LICENSE, and read the part about language data before redistributing anything.

## What works

* Eleven languages: every module openevv has -- US and British English, Castilian and Mexican Spanish, French of France and of Canada, German, Italian, Polish and Japanese -- and Russian, which openevv has not got and which is spoken here by its US English module from phonemes. "Russian" below says how.
* Eight preset voices: Reed, Shelley, Bobby, Rocko, Glen, Sandy, Grandma, Grandpa. Offered through the Android voice API, so they appear separately.
* All eight voice parameters: speed, pitch, inflection, head size, roughness, breathiness, volume, gender.
* Speech rate and pitch from the system settings.
* Pronunciation dictionaries.
* Android 6 and later. arm64-v8a, armeabi-v7a, x86_64.

`app/src/androidTest` runs on a device. 82 checks.

## Turning it on

TalkBack settings, Text-to-speech settings, Preferred engine, Eloquence (openevv). The gear next to it opens the voice settings.

## Turning it off

TalkBack settings, Text-to-speech settings, Preferred engine, Google.

## Languages

All eleven are in the APK. Nothing is downloaded and nothing is missing, so Android's own language list for the engine is the list below and each one has the same eight voices.

| language | locale | engine number |
| --- | --- | --- |
| US English | eng-USA | 0x00010000 |
| British English | eng-GBR | 0x00010001 |
| Castilian Spanish | spa-ESP | 0x00020000 |
| Mexican Spanish | spa-MEX | 0x00020001 |
| French | fra-FRA | 0x00030000 |
| Canadian French | fra-CAN | 0x00030001 |
| German | deu-DEU | 0x00040000 |
| Italian | ita-ITA | 0x00050000 |
| Japanese | jpn-JPN | 0x00080000 |
| Polish | pol-POL | 0x00110000 |
| Russian | rus-RUS | 0x00120000 |

Which one speaks is the caller's to say. A screen reader asks by locale and the voice name carries it too, so `pol-POL-Reed` is Reed speaking Polish. A locale that matches on the language but not the country still gets an answer: text marked Australian is read by the first English module in the build rather than refused.

Text reaches the engine as bytes in the language's own code set, and the four answers are in `EngineText`. Eight of the eleven read the Windows Western set, one byte a character, and anything outside it is flattened first: curly quotes become plain ones, an accent that decomposes gives up its base letter so Dvořák is readable, and everything else becomes a space rather than a noise. Polish has eight letters that set has no room for, so the module declares them and the engine converts its text from UTF-8; everything but those eight is flattened as before. Japanese has a romanizer in front of the engine and that reads Shift-JIS, which is fixed when the instance is made and cannot be moved afterwards.

Russian is the eleventh code set and the engine's own business: the text goes over as UTF-8 and the engine spells it out, which "Russian" below describes.

Polish capitals reach the letter rules as lowercase, which is openevv's and is right for every sound. Nothing here notices a capital.

The settings screen has a Preview language row, so a voice can be tuned while listening to the language it will be heard in, and each language has a sentence of its own to say. That row changes the preview and nothing else. What a screen reader hears is the language it asked for.

## Russian

openevv has no Russian module and nothing to lift one from: IBM never shipped one, and no module in the tree reads Russian spelling for itself. What US English has is the fullest phonology and prosody of the eleven, and an annotation that takes a pronunciation in its own phoneme alphabet with the stress of each syllable marked: `` `[.0x.1bWt] `` is about. So Russian is the English module speaking phonemes the engine writes for it.

All of it is in the engine, and `native/openevv` is [the fork of openevv](https://github.com/nikkov199525/openevv) that has it, kept level with upstream with Russian on top. That is two pieces. Its build writes `lang/ruru` with `tools/russian/make-ruru.py` when `LANGS` names it, a copy of `lang/enus` under Russian's own number, family eighteen, the last of the eighteen the engine's tables have room for. A copy rather than English under a second name, because the engine knows a language by its module: Russian has its own eight voices, its own dictionaries in force and its own row in every list a screen reader shows. And `src/eci/synth/eci_russian.c` writes each Russian word as one of those annotations before the module sees it:

* the stress, from the mark over a vowel when the text has one, from ё, from a table of 570,000 word forms, and then by rule: adjectives and nouns in -ие and -ия on the last vowel of the stem, the infinitive and the past tense on their suffix, -ист, -изм, -ант, -ок and -ация on themselves, and the penultimate syllable when nothing says otherwise; prepositions, conjunctions and particles among other words are not stressed at all;
* the vowels reduced as Russian reduces them: молоко is малако, unstressed е and я are и, and ы, with и after ж, ш and ц, is the module's X -- the reduced vowel of roses, which is the same sound;
* voicing assimilated from the right, so сделать is zdelat, к другу is gdrugu and код ends in a t, and a consonant softened by е, ё, ю or я given the glide y after it, which is as near as an English voice comes to a soft consonant: нет is nyet;
* р is the module's flap, which in `lang/ruru` is placed at the trill's locus that Italian's and so Polish's r uses rather than a d's, so it is what Polish r is: one dip in the voicing at a trill's formants. Before a vowel that is all it is; at the end of a word or before a consonant a tap has nothing to release into and was lost, so the module's r follows it, placed at the same locus. Latin r is the module's own. о and ё are the module's o made a plain vowel in `lang/ruru` rather than the diphthong of boat; ч is the module's ch, ш and ж its sh and zh, щ a soft sh before a vowel and шч elsewhere, х its h before a vowel, and k with the fricative below before л, р, м, н and в, where the module drops an h too; anywhere else the module has nothing that sounds like х -- it drops an h at the end of a syllable, k made них ник, and its th was heard as f even made louder -- so `lang/ruru` gives х a phoneme of its own, Q, a velar fricative the English module declares and never says, and speaks it as its sh said at the velar place with every noise band moved down two, from where sh has its energy to where х has it, which is what the ear chose of three settings. English th in Latin text is the module's own and sounds as it does in English;
* что is што, the genitive -ого and -его are -ово and -ево, -тся and -ться are ца, сч is щ, and the consonants Russian does not say in здравствуйте, солнце, сердце and поздно are dropped;
* numbers are said in Russian words, cardinals up to a trillion with тысяча, миллион and миллиард agreeing with what they count, ordinals written with a hyphen and an ending (1-го мая is первого мая, 5-й is пятый), decimals with запятая or точка, thousands grouped with a space, dates in parts, and a number that starts with a nought a digit at a time;
* a consonant on its own is said by its Russian name (б is бэ, й is и краткое), which is what a screen reader moving by character wants, except в, к and с in front of a word, which are prepositions and are said as part of it; a word with no vowel, СССР, is spelled.

A stress can be written into the text: an acute accent over the vowel, as careful texts do, or a plus in front of it, as Silero and RUAccent write it -- з+амок and зам+ок. That is also how a stress is corrected without touching the engine: an NVDA speech dictionary entry replacing замок with зам+ок.

Three things the English module does to any phonemes had to be stepped round. It starts a vowel from silence at full strength, half its peak within a millisecond, which in English is the hard attack of own and in Russian was heard as о clicking at the start of он, окно and это; for Russian alone a start from ten milliseconds of silence is faded in over five, which takes the click and leaves the vowel. It flaps a t or a d that starts an unstressed syllable, so это came out as eda, and Russian never does that; a secondary stress is what it does not flap before, so such a syllable is given one. And it drops an h at the end of a syllable, which is where х went.

Latin text inside Russian is read as English, which is what it usually is. The annotations are read whatever the caller asked for, the way a filter turns them on, and a backquote the caller wrote while asking for none becomes a space rather than a command.

What says it is exact: a Russian word comes out byte for byte the samples the English module gives for the phonemes it was turned into, and the instrumented tests hold that on a device.

The table is every inflected form of every noun, adjective and verb in [OpenRussian](https://en.openrussian.org)'s dictionary, from its [published data](https://github.com/Badestrand/russian-dictionary), with the vowel each is stressed on; where two words share a spelling the commoner one is kept, so замок is a castle. A word written with ё is very often typed with е, so еще and идет are in it too, marked to be read with their ё wherever the е spelling is not a word of its own. `tools/russian/openrussian.py` in the engine made `openrussian.tsv.gz` out of that data, and `tools/russian/stress.py` packs it into the engine at build time beside `tools/russian/stress.txt`, which is ours, wins where the two disagree, and is a word to a line with its stressed vowel in capitals. Packed, it is 2.7 megabytes in each library.

OpenRussian's data is under CC BY-SA 4.0, so the table and the part of each library it becomes are too; `tools/russian/NOTICE` in the engine says so and is what to keep beside them. Everything else here is MIT.

When it is there, [RuLex](https://github.com/poretsky/rulex), Igor Poretsky's pronunciation dictionary for Russian, comes first. It gives a word its pronunciation as well as its stress -- что is што, солнце сонце, компьютер компьютэр, the э marking a hard consonant -- and leaves a function word unstressed. A word it does not have has its prefixes cut off by RuLex's own classifiers and is looked up again; failing that the table above has its say, then RuLex's general rules, which place the stress from the shape of the word, and only then the endings. A word of more than one syllable that RuLex leaves unstressed is one it will not decide, замок or дома, and takes its stress from the table. Measured against RuLex's own 423,000 word forms, the OpenRussian table covers 58% of them and agrees on 98.9% of those; on the rest the endings alone were right 47% of the time.

RuLex is GPL, so it is a file of its own and never compiled in: the engine's build writes `rulex.dat` with `tools/russian/rulex.py` out of RuLex's sources in `tools/russian/rulex/`, the APK carries it as an asset that the app copies out and tells the engine about, and the NVDA add-on carries it beside the library, where the engine looks for it. `EVV_RULEX` in the environment names it anywhere else. Without it the engine speaks Russian as it did before. `tools/russian/NOTICE` says what is under which licence. The engine's rules are a small regular expression matcher of its own for RuLex's patterns, since the Windows builds have none.

A word that is in none of these and is caught by no rule is stressed on the penultimate syllable.

## NVDA

The voice settings have a box for whether the synthesiser reads punctuation, and it is off. The engine names in English almost every symbol left in the text -- brackets, quotation marks, @, #, the slash -- and reads a full stop or a comma that stands on its own as dot and comma, whatever language it is speaking. NVDA has its own setting for how much punctuation to say, in the reader's own language, so with the box off those characters are taken out before the engine sees them, dashes and ellipses become a pause, and the marks that make a pause are kept against their word. Ticking it hands the engine the text as it came.

openevv's NVDA add-on carries every language in the library it ships, so with Russian in the library NVDA offers Russian - Reed and the rest, and a document's own language switches to it when a passage is marked ru. The add-on is openevv's own, built by its own Makefile, and carries `rulex.dat` and its licence beside the library:

```
make -C native/openevv nvda LANGS="lang/enus lang/engb lang/eses lang/esus lang/frfr lang/frca lang/dede lang/itit lang/plpl lang/ruru lang/jajp"
```

That wants what openevv's `make nvda` always wants: a mingw cross compiler for both bitnesses, since NVDA loads a 32-bit `eci32.dll` when it hosts the engine out of process and a 64-bit `eci.dll` in process. `docs/windows.md` in openevv says the rest. The add-on lands in `native/openevv/build`.

## Translations

The settings screen is in English and Russian, the Russian in `app/src/main/res/values-ru`, and Android picks whichever the phone is set to. The NVDA add-on is the same: its labels and the names of its languages are in `nvda/addon/locale/ru` in the engine, compiled into the catalog NVDA reads when the add-on is packed. Until a voice has been chosen the add-on speaks the language NVDA itself is in, where the library has it, rather than the library's first language, which is US English and reads Russian as a stream of symbol names.

## Building

Needs the Android SDK, NDK r23 or later, JDK 17, Python 3 and bash. No Android Studio, and the wrapper fetches its own Gradle.

```
git submodule update --init --recursive
export ANDROID_NDK_HOME=/path/to/ndk
./gradlew installDebug
./gradlew connectedDebugAndroidTest
```

First build is about half an hour on sixteen cores. Most of that is the engine writing each language's rules out of text with Python and decompiling them into C, which is per language rather than per ABI, so only the first ABI pays for it; after that it is about three minutes per ABI. `-Pevvdroid.langs=lang/enus` is one language and about four minutes.

Eleven languages make `libevvjni.so` about 48 MB, and a library is stored uncompressed so that it is mapped straight out of the APK rather than unpacked at install. So one APK per ABI is the default rather than one holding all three.

| property | default | effect |
| --- | --- | --- |
| `evvdroid.abis` | `arm64-v8a,armeabi-v7a,x86_64` | which ABIs to build |
| `evvdroid.rules` | `c` | `bytecode` skips the decompiling, so minutes rather than half an hour, and runs at under half the speed |
| `evvdroid.langs` | all eleven | language modules to link in, space separated, first one wins a caller that names none |
| `evvdroid.abiSplits` | `true` | one APK per ABI, plus a universal one |

`-Pevvdroid.abis=arm64-v8a -Pevvdroid.rules=bytecode` is the fast loop for working on the Kotlin. Compiled rules are the default because interrupting and restarting costs about a third of what it costs interpreted, and a screen reader interrupts constantly.

A change to openevv that upstream has not taken goes into the fork `native/openevv` points at, and the submodule is moved to it. A file under `native/patches` would still be laid on the engine at build time, but there are none.

## Settings

Launcher icon, or the gear beside the engine in the text-to-speech settings. Sliders for the seven numeric voice parameters, plus gender, voice, preview language, sample rate, pauses, phrase prediction, abbreviations and dictionaries. Nothing speaks on its own: a setting takes effect when you set it, and "Speak a sample" is how you hear it.

Each row is one control. A label, a bar and a number would be three stops for a screen reader, so each row merges into a single node with `clearAndSetSemantics`. It reads as "Speed, 20 percent" and adjusts in place.

Values are percentages. The engine uses three scales (gender is 0 or 1, speed runs to 250, the rest to 100), so a raw number means nothing without knowing which.

The row takes the keyboard and the bar inside it does not. A Compose slider keeps a focus of its own, which no screen reader can see once the semantics belong to the row, and it answers the End key by going to the maximum. So the bar is out of the tab order and the row answers the keys itself.

An arrow moves one percent. TalkBack adjusts any slider by a twentieth of the range it is given, and the number of steps a Compose slider declares never reaches the platform, so TalkBack's own arrow asks for five. Five percent of the speed scale is the difference between too slow to bear and faster than was wanted, so the row reads a request to move as a request to move one.

Speech rate from the system is a multiplier, so it is treated as one. The engine's speed scale is far from linear: speed 100 speaks 2.7 times the default rate and speed 200 speaks 16.6 times. A measured curve converts between the two rather than multiplying, so 200 percent is about twice as fast and very high rates saturate at the engine's ceiling.

Voice settings belong to the voice. Set Reed's pitch to 55, go to Glen and come back, and Reed is still at 55. Only "Reset voice to defaults" forgets it, and only for the voice you are on.

Speed is the listener's, not the voice's. Six presets ship at engine speed 50 and Glen and Sandy ship at 70, half again as fast, so a preset brings its other seven settings and leaves the speed alone.

Sliders start at the voice's own values. Each preset carries its own head size, inflection and volume: Reed is inflection 30 and volume 92, Bobby is 35 and 90. A parameter you have not set is never sent to the engine, so a fresh install sounds like Eloquence always has.

## Pauses

Eloquence pauses generously, which reads well in prose and badly in a screen reader where most utterances are a few words. The engine has no setting for it but it takes an annotation: `` `p `` and a number is a pause of that many milliseconds, and one placed in front of a punctuation mark replaces the pause that mark would have had.

Three choices. Do not shorten leaves the engine alone. Shorten at end of text trims only the gap after the last thing said. Shorten all pauses trims punctuation as well, which is the default and takes a sentence with commas to 65 percent of its original time.

The rule is davidacm's, from the [NVDA IBMTTS driver](https://github.com/davidacm/NVDA-IBMTTS-Driver), which calls these JAWS-like pauses. A mark counts when it follows a letter, digit or space and is followed by whitespace, a slash or the end, which leaves the point in 3.14, the colon in 2:30 and the comma in 1,024 alone.

## Voice, phrase prediction, abbreviations

The voice is whichever the settings screen says. A caller that names a voice does not override it, because a client holds on to the Voice object it was given: a screen reader that connected while Reed was selected goes on asking for Reed however many times the setting changes underneath it. The name is still what decides the language.

Phrase prediction is the engine guessing where the phrase boundaries in a sentence are and shaping the intonation to match. That reads well in prose and gets in the way in a screen reader, where a line is usually a fragment and the guess is wrong, so it is off. It is an annotation rather than a parameter, so it goes out with every utterance.

Expand abbreviations is `eciDictionary`, which is inverted in the engine: zero turns it on. Off by default, and re-sent with every utterance, since it is the one setting with nothing to say whether something else has moved it.

## Dictionaries

Three rows pick a file each for the main, root and abbreviation volumes. "Remove dictionaries" clears all three.

The files people share are text: a word, a tab, then either a respelling (`dee oe jeigh`) or a pronunciation in the engine's own alphabet (`` `[Ekspoz1e] ``). [mohamed00/altibmttsdictionaries](https://github.com/mohamed00/altibmttsdictionaries) has the English pair.

The engine's own loader will not read them. `eciLoadDict` answers `eciDictAccessError` for every one because it expects IBM's saved binary form, so the text is parsed here and the engine is taught a word at a time through `eciUpdateDict`. 3000 entries take about 450 ms, once, when the service starts. They reload only when the files change.

A picked file is copied into app storage. A content URI belongs to whichever app supplied it, may not be readable later, and the speech service reads it from another process.

No dictionary is shipped here. See LICENSE.

## Interrupting

A screen reader wants each string to cut off the one before it. Two things make that work.

The engine cannot abandon an utterance, so long text is handed over in sentence-sized pieces and asking for silence waits out a piece rather than a whole paragraph. The rule for where a piece may end is openevv's, ported from its NVDA driver: a sentence end is free because the engine already pauses there, anywhere else costs about 0.4s, so a full stop has to prove itself. A word with a dot inside it is an abbreviation, and a short capitalised word is an initial.

Synthesis is paced to real time. The engine produces 1.3s of audio in 12 ms and Android queues every byte of it, so a reader swiping quickly would leave five or six items queued while the first is still playing and lose all of them when it asks for silence. Output runs no more than 300 ms ahead. Measured on the same text: 2229 ms of audio handed over in 700 ms of speaking without that limit, about 1000 ms with it.

## Wear OS

Runs on a watch. Tested on a Pixel Watch 4, which is armeabi-v7a.

A Pixel Watch has no USB, so adb is wireless. On the watch: Settings, System, About, Versions, tap Build number seven times. Then Developer options, ADB debugging on, Wireless debugging on, Pair new device. Watch and computer on the same Wi-Fi, with the watch's Wi-Fi actually on rather than dropped for Bluetooth.

```
adb mdns services                 # the watch announces both ports
adb pair 192.168.x.x:PAIRPORT     # then the six digits on screen
adb connect 192.168.x.x:PORT
adb -s SERIAL shell getprop ro.product.cpu.abilist
./gradlew installDebug -Pevvdroid.abis=armeabi-v7a
```

## Layout

```
native/openevv          the engine, a submodule: the fork of openevv with Russian in it
native/android.mk       two make targets openevv has no reason to carry
native/build-native.sh  cross-compiles the engine, links libevvjni.so
native/tools            drives openevv's own test gate over adb
app/src/main/cpp        JNI bridge
app/src/main/kotlin     the service, engine wrapper, settings
```

The engine is built by its own Makefile rather than CMake, because that Makefile generates the language rules with Python first. Gradle runs the script and picks the result up as prebuilt `jniLibs`.

Everything links into one shared object, `libevvjni.so`. Android only packages files named `lib*.so`, and openevv's own library records a soname of `libeci.so.1`, which nothing would then find.

`native/tools/probe-on-device.sh` stands in for openevv's own probe, so its gate, `test/matrix.sh check enus`, drives an Android build over adb unchanged. That is 98 cases comparing sample hashes and reported answers against baselines recorded on x86. Every other language has a gate of its own under the same name, and `test/matrix.sh` walks all ten.

## Licence

MIT for the Kotlin, the C bridge, the build and the tests. See LICENSE.

Not the engine. openevv is MIT under its own copyright and is fetched as a submodule, not copied here.

Not the language data openevv carries. That is transcribed from IBM's Embedded ViaVoice objects and is IBM's. `native/openevv/NOTICE` says what it is and whose it is.

Not the Russian data the engine carries in `tools/russian`: the stress data is OpenRussian's and is CC BY-SA 4.0, as is what the build makes of it, and RuLex is GPL-2, which is why `rulex.dat` is an asset the app reads rather than code it links. `native/openevv/tools/russian/NOTICE` says what each is and what passing it on asks.

No pronunciation dictionary is included. Those belong to whoever wrote them, and the best known collection carries no licence at all, so it is linked rather than copied. The test fixture here is written for the purpose.
