# Vanilla Core - GUI Configuration System Testing Guide

## Overview
This document outlines the testing procedures for the new GUI configuration system added to Vanilla Core.

## Test Environment Requirements

### Server Requirements
- Minecraft Server: Paper/Spigot 1.21.1+
- Java Version: 21+
- Vanilla Core: Latest build from feature/gui-config-system branch
- Test World: Fresh creative world recommended

### Testing Accounts
- Admin account with `vanillacore.admin` permission
- Regular player account (for permission testing)

## Component Testing

### 1. GUI Framework Components

#### AnvilInputGUI
- [ ] Opens successfully without errors
- [ ] Shows prompt message in chat
- [ ] Accepts valid text input
- [ ] Accepts valid numeric input
- [ ] Rejects invalid input with appropriate error message
- [ ] Number validator (min/max) works correctly
- [ ] Non-empty validator works correctly
- [ ] Cancel functionality works
- [ ] Returns to previous menu after input
- [ ] Handles empty input appropriately

#### BooleanToggleGUI
- [ ] Opens with correct layout (27 slots)
- [ ] Shows enable option (green wool, slot 11)
- [ ] Shows disable option (red wool, slot 15)
- [ ] Shows cancel option (barrier, slot 22)
- [ ] Highlights current selection correctly
- [ ] Clicking enable calls callback with true
- [ ] Clicking disable calls callback with false
- [ ] Clicking cancel returns without changing
- [ ] Sound plays on selection
- [ ] Closes inventory properly

#### ListEditorGUI
- [ ] Opens with empty list
- [ ] Opens with existing items
- [ ] Add item button works
- [ ] Remove item (left-click) works
- [ ] Move up (right-click) works
- [ ] Move down (shift-right-click) works
- [ ] Clear all button works
- [ ] Save changes persists to config
- [ ] Cancel discards changes
- [ ] Pagination works for large lists (>28 items)
- [ ] Material items display correctly

#### ItemSelectorGUI
- [ ] Opens with all materials displayed
- [ ] Categories filter correctly:
  - [ ] All Items
  - [ ] Blocks
  - [ ] Items
  - [ ] Combat
  - [ ] Tools
  - [ ] Food
  - [ ] Redstone
- [ ] Search functionality works
- [ ] Clear search works
- [ ] Pagination works
- [ ] Selecting an item calls callback
- [ ] Cancel returns without selection
- [ ] Material icons display correctly

#### ConfigMenuGUI (Base Class)
- [ ] Tracks pending changes correctly
- [ ] isDirty flag updates when changes made
- [ ] Save button enabled only when dirty
- [ ] Save persists changes to config.yml
- [ ] Discard clears pending changes
- [ ] Config reload happens after save
- [ ] Feature reload happens after save
- [ ] Back button returns to main menu
- [ ] Help button shows contextual help

### 2. Feature Configuration GUIs

#### OnePlayerSleepConfigGUI
- [ ] Opens from right-click on feature item
- [ ] Feature toggle works
- [ ] Sleep message editor opens
- [ ] Sleep message saves correctly
- [ ] Skip message editor opens
- [ ] Skip message saves correctly
- [ ] "empty" keyword disables messages
- [ ] Help button shows feature info
- [ ] Save applies changes to config
- [ ] Changes persist after server restart

#### ItemExplosionImmunityConfigGUI
- [ ] Opens from right-click on feature item
- [ ] Feature toggle works
- [ ] Displays feature information
- [ ] Save applies changes
- [ ] Help button works

### 3. Integration Testing

#### Main Menu Integration
- [ ] Main menu still opens with `/vanilla`
- [ ] Feature items show "Right Click: Configure"
- [ ] Left-click still toggles features
- [ ] Right-click opens config GUI
- [ ] Navigation between menus works
- [ ] No duplicate event handlers

#### Config Persistence
- [ ] Changes save to config.yml
- [ ] Config file format remains valid YAML
- [ ] Config loads correctly on restart
- [ ] No data loss on save
- [ ] Concurrent saves don't corrupt file

#### Permission Testing
- [ ] Admin permission required to open `/vanilla`
- [ ] Admin permission required to configure features
- [ ] Non-admins see appropriate error message
- [ ] OP players have access by default

### 4. Edge Case Testing

#### Invalid Input Handling
- [ ] Empty string input handled correctly
- [ ] Very long strings don't crash (>1000 chars)
- [ ] Special characters in messages work
- [ ] Color codes (&sect;) work in messages
- [ ] Unicode characters handled
- [ ] Numbers outside range rejected
- [ ] Negative numbers handled correctly
- [ ] Decimal numbers handled (if applicable)

#### GUI State Management
- [ ] Closing GUI mid-edit discards changes
- [ ] Opening multiple GUIs doesn't break state
- [ ] Rapid clicking doesn't duplicate actions
- [ ] Server restart clears GUI state
- [ ] Player disconnect clears GUI state
- [ ] Switching worlds works correctly

#### Concurrent Access
- [ ] Multiple admins can configure simultaneously
- [ ] Changes don't interfere with each other
- [ ] Last save wins (expected behavior)
- [ ] No race conditions on config save

### 5. Error Handling

#### Console Errors
- [ ] No errors on plugin enable
- [ ] No errors when opening any GUI
- [ ] No errors when closing any GUI
- [ ] No errors when saving config
- [ ] No errors when loading config
- [ ] No errors on player disconnect in GUI

#### Graceful Degradation
- [ ] Missing config values use defaults
- [ ] Corrupted config regenerates
- [ ] Missing permissions show friendly error
- [ ] Inventory full doesn't break GUI
- [ ] Network lag doesn't duplicate actions

### 6. Performance Testing

#### Resource Usage
- [ ] No memory leaks after repeated GUI opens/closes
- [ ] CPU usage remains normal
- [ ] No TPS drops when multiple players use GUI
- [ ] Config save is fast (<100ms)
- [ ] GUI opens instantly (<50ms)

#### Stress Testing
- [ ] 10+ players using GUI simultaneously
- [ ] Rapid open/close cycles (100+ times)
- [ ] Large lists (1000+ items) in ListEditorGUI
- [ ] Long messages (500+ characters)

### 7. Visual/UX Testing

#### Menu Layouts
- [ ] Items are logically organized
- [ ] Important buttons are easily accessible
- [ ] Color coding is consistent:
  - Green = enabled/success
  - Red = disabled/danger
  - Yellow = warning
  - Gray = disabled/inactive
- [ ] Icons match functionality
- [ ] Lore is clear and helpful

#### Messages
- [ ] Success messages are clear
- [ ] Error messages are helpful
- [ ] Confirmation messages are appropriate
- [ ] Chat spam is minimal
- [ ] Color formatting is consistent

#### Sound Effects
- [ ] Success sound plays on save
- [ ] Selection sound plays on click
- [ ] No annoying/repetitive sounds
- [ ] Volume levels are appropriate

## Testing Procedure

### Daily Test Cycle (During Development)

1. **Start fresh**
   - Delete config.yml
   - Start server
   - Verify clean generation

2. **Basic functionality**
   - Open `/vanilla` menu
   - Test 2-3 feature config GUIs
   - Make changes and save
   - Verify config.yml updated

3. **Config persistence**
   - Restart server
   - Open GUIs again
   - Verify changes persisted

4. **Edge cases**
   - Test invalid inputs
   - Test rapid clicking
   - Test cancel operations

### Pre-Commit Checklist

Before committing code:
- [ ] No console errors
- [ ] All implemented GUIs work
- [ ] Changes save to config
- [ ] Changes persist across restart
- [ ] Code follows style guide (no comments)
- [ ] Commit message is descriptive

### Pre-Release Testing

Before merging to main:
- [ ] Complete all component tests
- [ ] Complete all integration tests
- [ ] Complete all edge case tests
- [ ] Complete stress testing
- [ ] Get second person to test
- [ ] Update documentation
- [ ] Update CHANGELOG

## Bug Reporting Template

When finding a bug during testing:

```markdown
### Bug Report

**Component**: [Which GUI/feature]
**Severity**: [Critical/High/Medium/Low]

**Steps to Reproduce**:
1. 
2. 
3. 

**Expected Behavior**:

**Actual Behavior**:

**Console Output**:
```
[paste any errors]
```

**Screenshots**: [if applicable]

**Environment**:
- Server: Paper/Spigot version
- Java: version
- Plugin: commit hash
```

## Test Coverage Goals

- [ ] 100% of GUI components tested
- [ ] 100% of feature config GUIs tested
- [ ] 90%+ of edge cases tested
- [ ] All critical paths tested
- [ ] Performance benchmarks met

## Known Limitations

Document any known limitations discovered during testing:

1. **AnvilGUI limitations**: Bukkit anvil GUIs are limited; we use chat input as a workaround
2. **Item stacking**: Some GUI items may stack when they shouldn't; doesn't affect functionality
3. **Client-side**: Client-side resource packs may change item appearances

## Testing Notes

Keep track of testing sessions:

### Session 1: [Date]
- Tester: [Name]
- Focus: [What was tested]
- Results: [Pass/Fail, issues found]
- Notes: [Any observations]

### Session 2: [Date]
...

## Automated Testing Considerations

Future improvements could include:

- Unit tests for config save/load
- Mock player testing for GUI interactions
- Integration tests with real server
- Automated regression testing

## Sign-Off

Testing complete when:
- [ ] All checklist items marked complete
- [ ] No critical bugs remaining
- [ ] Performance benchmarks met
- [ ] Documentation updated
- [ ] Code reviewed

**Tested by**: ________________  
**Date**: ________________  
**Status**: Pass / Fail / Needs Work
