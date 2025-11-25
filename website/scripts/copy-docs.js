const fs = require('fs');
const path = require('path');

const distDir = path.join(__dirname, '..', 'dist');
const outDir = path.join(__dirname, '..', 'out');
const docsDir = path.join(outDir, 'docs');

// Create docs directory in out folder
if (!fs.existsSync(docsDir)) {
  fs.mkdirSync(docsDir, { recursive: true });
}

// Copy all files from dist to out/docs
function copyRecursive(src, dest) {
  const exists = fs.existsSync(src);
  const stats = exists && fs.statSync(src);
  const isDirectory = exists && stats.isDirectory();
  
  if (isDirectory) {
    if (!fs.existsSync(dest)) {
      fs.mkdirSync(dest, { recursive: true });
    }
    fs.readdirSync(src).forEach(childItemName => {
      copyRecursive(
        path.join(src, childItemName),
        path.join(dest, childItemName)
      );
    });
  } else {
    fs.copyFileSync(src, dest);
  }
}

if (fs.existsSync(distDir)) {
  console.log('Copying Astro build from dist/ to out/docs/...');
  
  // Ensure out directory exists
  if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
  }
  
  copyRecursive(distDir, docsDir);
  console.log('✓ Docs copied successfully to out/docs/');
} else {
  console.error('Error: dist/ directory not found. Make sure Astro build completed successfully.');
  process.exit(1);
}

