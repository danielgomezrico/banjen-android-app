import 'package:banjen_flutter/home/player.dart';
import 'package:banjen_flutter/map_extensions.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
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

    return Scaffold(body: _buildButtons());
  }

  Widget _buildButtons() {
    final buttons = _buttonTexts.mapIndexed((index, text) {
      var color = _selectedButtonIndex == index ? Colors.green : Colors.white;

      return Expanded(
        child: FlatButton(
          onPressed: () => onClick(index),
          color: color,
          child: Text(text),
        ),
      );
    }).toList();

    return Column(
        crossAxisAlignment: CrossAxisAlignment.stretch, children: buttons);
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
