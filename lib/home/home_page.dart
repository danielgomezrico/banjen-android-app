import 'package:banjen_flutter/home/player.dart';
import 'package:banjen_flutter/map_extensions.dart';
import 'package:flutter/material.dart';

class HomePage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => HomeState();
}

class HomeState extends State<HomePage> {
  var _buttonTexts = ["4 - D", "3 - G", "2 - B", "1 - D"];
  var _selectedButtonIndex = -1;
  var _player = SoundPlayer();

  @override
  Widget build(BuildContext context) {
    var selectedStatus = _buttonTexts.map((e) => false).toList();
    if (_selectedButtonIndex != -1) {
      selectedStatus[_selectedButtonIndex] = true;
    }

    var screenSize = MediaQuery.of(context).size;

    return Scaffold(
        appBar: AppBar(
          title: const Text('ToggleButtons'),
        ),
        body: Container(
            width: double.infinity,
            height: double.infinity,
            child: ToggleButtons(
                constraints: BoxConstraints.expand(
                  height: screenSize.height,
                  width: (screenSize.width / _buttonTexts.length) - 2,
                ),
                isSelected: selectedStatus,
                onPressed: onClick,
                children: _buttonTexts
                    .mapIndexed((index, text) => Text(text))
                    .toList())));
  }

  void onClick(index) {
    var shouldPlay = _selectedButtonIndex != index;

    setState(() {
      if (shouldPlay) {
        _selectedButtonIndex = index;
      } else {
        _selectedButtonIndex = -1;
      }
    });

    if (shouldPlay) {
      _player.play(index);
    } else {
      _player.stopPlayback();
    }
  }
}
