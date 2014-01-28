SeasonNarrative
===============

Audiovisual narrative of the four seasons

===============

To Do:

** Splice Vivaldi's 4 seasons into one .ogg or .mp3

** Create keyframing class. Class has array of keyframes.
Keyframes have timestamp, and parameters associated with them.
Keyframing should take a timestamp, and linearly interpolate between parameters
in adjascent keyframes.

Ex:
  Keyframe1(time = 00, branchLength = 05, color1R = 123)
  Keyframe2(time = 10, branchLength = 10, color1R = 100)
  Keyframe3(time = 20, branchLength = 30, color1R = 200)
  Keyframe4(time = 24, branchLength = 33, color1R = 140)

query time = 15 should return
Keyframe(time = 15, branchLength = 20, color1R = 150)

** Manually create keyframes

** Play music

** (Optional) modulate keyframe parameters with characteristics of the music via FFT

