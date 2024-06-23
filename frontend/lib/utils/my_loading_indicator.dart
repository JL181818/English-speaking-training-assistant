import 'package:flutter/material.dart';

class Loading extends StatelessWidget {
  final String message;

  const Loading({Key? key, this.message = 'Loading...'}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.black.withOpacity(0.5),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            CircularProgressIndicator(),
            SizedBox(height: 16),
            Text(
              message,
              style: TextStyle(color: Colors.white),
            ),
          ],
        ),
      ),
    );
  }
}
