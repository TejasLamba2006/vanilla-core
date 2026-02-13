# Vanilla Core Documentation

This folder contains the documentation website for Vanilla Core, built with [Docusaurus](https://docusaurus.io/).

## Development

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

```bash
cd docs
npm install
```

### Local Development

```bash
npm start
```

This starts a local development server at `http://localhost:3000`. Most changes are reflected live without having to restart the server.

### Build

```bash
npm run build
```

This generates static content into the `build` directory that can be deployed to any static hosting service.

## Deployment

### GitHub Pages

```bash
GIT_USER=<Your GitHub username> npm run deploy
```

### Vercel

1. Connect your GitHub repository to Vercel
2. Set the root directory to `docs`
3. Build command: `npm run build`
4. Output directory: `build`

### Custom Domain

1. Add your domain in the hosting provider
2. Update `docusaurus.config.js`:

   ```js
   url: 'https://yourdomain.com',
   ```

## Structure

```
docs/
├── docs/                # Documentation pages
│   ├── intro.md
│   ├── installation.md
│   ├── commands.md
│   ├── permissions.md
│   ├── configuration.md
│   ├── gui-reference.md
│   ├── faq.md
│   └── features/        # Feature documentation
├── src/
│   ├── css/
│   │   └── custom.css   # Custom styling
│   └── pages/
│       └── index.js     # Homepage
├── static/
│   └── img/             # Images and logos
├── docusaurus.config.js # Site configuration
├── sidebars.js          # Sidebar navigation
└── package.json
```

## Contributing

1. Create a new branch
2. Make your changes
3. Test locally with `npm start`
4. Submit a pull request

## License

Same as the main project.
