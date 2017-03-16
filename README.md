# motion_profile
A basic path generation program for skid steer robots.

Currently built as an Eclipse project.

## Usage

Run `OfflineGenerator`

### Spline Interface

Spline interface controls:

+ `Mouse Wheel` - zoom
+ `Middle Click + Drag` - pan

#### `add` mode

+ `Left Click` - add waypoint

#### `edit` mode

The blue circle represents position,
the red diamond represents a velocity vector,
and the green triangle represents an acceleration vector.

+ `Left Click + Drag` - move control handle
+ `Right Click` - reverse the robot travel direction following that waypoint

#### `delete` mode

+ `Left Click` - delete waypoint

### Profile Interface

The top graph displays translational information, and the lower graph displays rotational information.

+ `Reverse Translation` - reverses the translation information on the CSV output.
+ `Reverse Rotation` - reverses the rotation information on the CSV output.
+ `Zero Start Velocity & Acceleration` - forces the velocity and acceleration at the start time to zero (usually desired).
+ `Zero End Velocity & Acceleration` - forces the velocity and acceleration at the end time to zero (usually desired).
