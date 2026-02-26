# Contributing to Vanilla Core

First off, thank you for considering contributing to Vanilla Core! It's people like you that make this plugin better for everyone.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Coding Guidelines](#coding-guidelines)
- [Pull Request Process](#pull-request-process)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Features](#suggesting-features)

## Code of Conduct

This project follows a simple code of conduct:

- Be respectful and constructive
- No harassment or discrimination
- Focus on what's best for the community
- Show empathy towards other contributors

## How Can I Contribute?

### Reporting Bugs

Before creating a bug report:

- Check the [existing issues](https://github.com/TejasLamba2006/vanilla-core/issues)
- Check the [documentation](https://vanillacore.tejaslamba.com)
- Make sure you're using the latest version

When reporting:

- Use the bug report template
- Include server version, plugin version, and Java version
- Provide clear reproduction steps
- Include relevant config sections
- Include error logs if applicable

### Suggesting Features

Before suggesting a feature:

- Check if it aligns with Vanilla Core's purpose (SMP server management)
- Check existing feature requests
- Consider if it can be implemented modularly

When suggesting:

- Use the feature request template
- Explain the problem you're trying to solve
- Describe your proposed solution
- Explain why this would benefit other servers

### Contributing Code

We love pull requests! Here's how you can contribute:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Make your changes** (follow our coding guidelines)
4. **Test thoroughly**
5. **Commit your changes** (`git commit -m 'Add amazing feature'`)
6. **Push to your fork** (`git push origin feature/amazing-feature`)
7. **Open a Pull Request**

## Development Setup

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- Git
- A Minecraft server running Paper/Spigot 1.21.1+

### Building from Source

```bash
# Clone the repository
git clone https://github.com/TejasLamba2006/vanilla-core.git
cd vanilla-core

# Build with Maven
mvn clean package

# The compiled JAR will be in target/
```

### Project Structure

```
vanilla-core/
├── src/main/java/com/tejaslamba/vanillacore/
│   ├── Main.java                  # Plugin entry point
│   ├── features/                  # Feature implementations
│   ├── listener/                  # Event listeners
│   ├── manager/                   # Manager classes
│   ├── menu/                      # GUI system
│   └── utils/                     # Utility classes
├── src/main/resources/
│   ├── config.yml                 # Default configuration
│   ├── messages.yml               # Message templates
│   └── plugin.yml                 # Plugin metadata
└── docs/                          # Documentation site
```

## Coding Guidelines

### General Principles

1. **No Comments in Production Code**
   - Code should be self-documenting
   - Use clear, descriptive names instead of comments
   - Exception: Complex algorithms may have brief explanations

2. **Clean, Descriptive Names**

   ```java
   // ✅ Good
   public void disableNetheriteArmor(Player player) { }
   
   // ❌ Bad
   public void dna(Player p) { }
   ```

3. **Package Structure**
   - Follow: `com.tejaslamba.vanillacore.[feature].[type]`
   - Examples:
     - `com.tejaslamba.vanillacore.combat.listener`
     - `com.tejaslamba.vanillacore.ritual.manager`

4. **Feature Modularity**
   - All features must be toggleable via config
   - Features should not depend on each other
   - Each feature should have its own config section

### Code Patterns

#### Feature Implementation

```java
public class MyFeature extends Feature {
    public MyFeature(Main plugin) {
        super(plugin, "my-feature");
    }

    @Override
    public void enable() {
        // Register listeners
        // Load configuration
    }

    @Override
    public void disable() {
        // Cleanup
        // Unregister listeners
    }
}
```

#### Config Access

```java
// Check if feature is enabled
if (!plugin.getConfigManager().get().getBoolean("features.my-feature.enabled")) {
    return;
}

// Get config value
int maxValue = plugin.getConfigManager().get().getInt("features.my-feature.max-value");
```

#### Event Listeners

```java
@EventHandler
public void onEvent(SomeEvent event) {
    // Early return if feature disabled
    if (!isFeatureEnabled()) {
        return;
    }
    
    // Your logic here
}
```

### Performance Considerations

1. **Early Returns** - Exit event handlers quickly when not needed
2. **Caching** - Cache config values after reload
3. **Concurrent Collections** - Use ConcurrentHashMap for thread-safe maps
4. **Proper Cleanup** - Clean up on player quit to prevent memory leaks

### Configuration Standards

1. **Default to Disabled** - All features start disabled
2. **Clear Documentation** - Add comments explaining options
3. **Sensible Defaults** - Choose reasonable default values
4. **Consistent Naming** - Use kebab-case for keys

Example:

```yaml
features:
  my-feature:
    enabled: false          # Feature must be explicitly enabled
    max-value: 10          # Default value should be sensible
    message: "§cWarning"   # Include color codes in defaults
```

## Pull Request Process

1. **Update Documentation**
   - Update feature docs in `docs/docs/features/`
   - Update `CHANGELOG.md` under `[Unreleased]`
   - Update README if needed

2. **Ensure Quality**
   - Code follows style guidelines
   - No new warnings or errors
   - Features are properly tested
   - Config changes are documented

3. **PR Description**
   - Use the PR template
   - Link related issues
   - Describe changes clearly
   - Include test results

4. **Review Process**
   - CodeRabbit AI will automatically review
   - Maintainer will review manually
   - Address feedback promptly
   - Keep the PR focused on one feature/fix

## Testing Your Changes

Before submitting:

1. **Local Testing**

   ```bash
   # Build the plugin
   mvn clean package
   
   # Copy to test server
   cp target/vanilla-core-*.jar /path/to/server/plugins/
   
   # Start server and test
   ```

2. **Test Cases**
   - Enable/disable feature via config
   - Test with various configuration options
   - Check for console errors
   - Test interaction with other features
   - Test with different Minecraft versions (if applicable)

3. **Performance Testing**
   - Test with multiple players (if applicable)
   - Monitor memory usage
   - Check for lag or performance issues

## Documentation

When adding features:

1. **Create Feature Doc** in `docs/docs/features/your-feature.md`
2. **Update Commands** if adding new commands
3. **Update Config Reference** with new options
4. **Add Examples** showing usage

## Release Process (Maintainers Only)

### Automated Publishing to Modrinth

The plugin is automatically published to Modrinth when a GitHub release is created:

1. **Update Version**
   - Update version in `pom.xml`
   - Update version in `.github/copilot-instructions.md`
   - Update `CHANGELOG.md` - move `[Unreleased]` items to new version section

2. **Create GitHub Release**
   - Go to [Releases](https://github.com/TejasLamba2006/vanilla-core/releases)
   - Click "Draft a new release"
   - Tag version: `vX.Y.Z` (e.g., `v1.3.1`)
   - Release title: `Vanilla Core X.Y.Z`
   - Copy the changelog for this version from `CHANGELOG.md`
   - Attach the built JAR file
   - Click "Publish release"

3. **Automatic Modrinth Upload**
   - GitHub Actions workflow triggers automatically
   - Builds the plugin from source
   - Extracts changelog for this version
   - Publishes to [Modrinth](https://modrinth.com/plugin/vanillacorewastaken)
   - Version info, changelog, and game versions are set automatically

4. **Required Secret**
   - Repository secret `MODRINTH_TOKEN` must be set
   - Token is obtained from [Modrinth Settings](https://modrinth.com/settings/pats)
   - Requires `PUBLISH_VERSION` scope

### Manual Publishing (Fallback)

If the automated workflow fails:

1. Build locally: `mvn clean package`
2. Go to [Modrinth project settings](https://modrinth.com/project/GH4H8ndx/versions)
3. Click "Upload version"
4. Fill in version details and upload JAR

### Versioning

Follow [Semantic Versioning](https://semver.org/):

- **MAJOR** (X.0.0): Breaking changes, major rewrites
- **MINOR** (x.Y.0): New features, non-breaking changes
- **PATCH** (x.y.Z): Bug fixes, small improvements

Examples:

- Bug fix: `1.3.1` → `1.3.2`
- New feature: `1.3.1` → `1.4.0`
- Breaking change: `1.3.1` → `2.0.0`

## Questions?

- Join our [Discord](https://discord.gg/7fQPG4Grwt)
- Check the [documentation](https://vanillacore.tejaslamba.com)
- Open a [discussion](https://github.com/TejasLamba2006/vanilla-core/discussions)

---

Thank you for contributing to Vanilla Core! 🎉
