# ItemAccessRestrictor | [物品存取限制器](./README.md)

<img src="./src/main/resources/logo.png" width = "128" height = "128" alt="ItemAccessRestrictor"/>

## Introduction

This is a Minecraft mod that adds a functional block: Item Access Restrictor.
It has no inventory itself, but when attached to a container, it can serve as the surface for inserting and extracting items.
You can configure input blocking, retain a certain number of items, etc. You can also use a comparator to read redstone signals.

## Configurations

| Name                                                   | Additional Information                                                                                                                                                                                                                                      | Scope                                                        | Default Value                        |
| :----------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :----------------------------------------------------------- | :----------------------------------- |
| Blocking input if target is not empty                  | Disabled slots and retained items are not included in the target.                                                                                                                                                                                           | Enabled, Disabled                                            | Enabled                              |
| Blocking input if receiving redstone signal            | None                                                                                                                                                                                                                                                        | Enabled, Disabled                                            | Enabled                              |
| Comparator Output Mode                                 | When set to 'Only count effective items and slots', it uses the same calculation method as typical containers like chest or hopper, with a minimum of 0 and a maximum of 15.                                                                                | Only count effective items and slots, Same with facing block | Only count effective items and slots |
| Input Stacking Limit                                   | Once the number of items inserted in a slot reaches the configured limit, no more items can be inserted to this slot; It will affect the calculation of the redstone signal output by a comparator.                                                         | Unlimited, 1–64                                              | Unlimited                            |
| Number of items retained                               | Once the number of items in a slot is reduced to the configured number, items can no longer be extracted from this slot; It will affect the calculation of the redstone signal output by a comparator.                                                      | No retain, 1–64                                              | No retain                            |
| Enable or disable insertion and extraction of the slot | When the number of slots facing the container changes, it will automatically switch back to the enabled; Disabled slots cannot be inserted into or extracted from, and they will not be included in the calculation for comparator output redstone signals. | Enabled, Disabled                                            | Enabled                              |

## Features

* Cannot be used nested.
* Unable to insert or extract items from the contact face.
* After enabling 'Blocking input if target is not empty', it supports inserting multiple types of items at once. The behavior of mods verified so far: AE2 can successfully send multiple items at once when crafting.

## Screenshot

![screenshot_en](docs/screenshot_en.png)
