import 'package:assets_audio_player/assets_audio_player.dart';

class SoundPlayer {
  var sounds = new List<int>.generate(4, (i) => i + 1)
      .map((e) => "assets/$e.mp3")
      .toList();

  AssetsAudioPlayer _audioPlayer;

  SoundPlayer() {
    _audioPlayer = AssetsAudioPlayer.newPlayer();
  }

  void play(int index) {
    if (index >= sounds.length) throw RangeError("No sound at $index");

    var audio = Audio(sounds[index]);

    _audioPlayer.open(audio, showNotification: true, loopMode: LoopMode.single);
  }

  void stopPlayback() {
    _audioPlayer.stop();
  }
}
