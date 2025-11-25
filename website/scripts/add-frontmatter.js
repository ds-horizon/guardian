const fs = require('fs');
const path = require('path');

// Function to extract title from first H1 heading
function extractTitle(content) {
  const match = content.match(/^#\s+(.+)$/m);
  return match ? match[1].trim() : 'Untitled';
}

// Function to quote title if needed for YAML
function quoteTitleIfNeeded(title) {
  // Quote title if it contains special YAML characters
  const needsQuotes = /[:@`|>#&!%*?{[\-]/.test(title) || title.includes('"') || title.includes("'");
  return needsQuotes ? `"${title.replace(/"/g, '\\"')}"` : title;
}

// Function to add frontmatter to a markdown file
function addFrontmatter(filePath) {
  let content = fs.readFileSync(filePath, 'utf-8');
  let needsUpdate = false;
  let pageTitle;
  
  // Check if already has frontmatter
  if (content.startsWith('---')) {
    // Extract existing frontmatter
    const frontmatterMatch = content.match(/^---\n([\s\S]*?)\n---\n/);
    if (frontmatterMatch) {
      const frontmatterContent = frontmatterMatch[1];
      const titleMatch = frontmatterContent.match(/^title:\s*(.+)$/m);
      
      if (titleMatch) {
        let existingTitle = titleMatch[1].trim();
        // Remove quotes if present
        existingTitle = existingTitle.replace(/^["']|["']$/g, '');
        
        // Check if title needs quoting
        const quotedTitle = quoteTitleIfNeeded(existingTitle);
        if (quotedTitle !== existingTitle || !titleMatch[1].match(/^["']/)) {
          // Rebuild frontmatter with quoted title
          const newFrontmatter = frontmatterContent.replace(/^title:\s*.+$/m, `title: ${quotedTitle}`);
          content = content.replace(/^---\n[\s\S]*?\n---\n/, `---\n${newFrontmatter}\n---\n`);
          needsUpdate = true;
          pageTitle = existingTitle;
        } else {
          return; // Already properly formatted
        }
      } else {
        // Has frontmatter but no title, extract from content
        const bodyContent = content.replace(/^---\n[\s\S]*?\n---\n/, '');
        const title = extractTitle(bodyContent);
        const filename = path.basename(filePath, '.md');
        pageTitle = title === 'Untitled' 
          ? filename.split(/(?=[A-Z])/).map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')
          : title;
        const quotedTitle = quoteTitleIfNeeded(pageTitle);
        const newFrontmatter = frontmatterContent + `\ntitle: ${quotedTitle}`;
        content = content.replace(/^---\n[\s\S]*?\n---\n/, `---\n${newFrontmatter}\n---\n`);
        needsUpdate = true;
      }
    }
  } else {
    // No frontmatter, add it
    const title = extractTitle(content);
    const filename = path.basename(filePath, '.md');
    pageTitle = title === 'Untitled' 
      ? filename.split(/(?=[A-Z])/).map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')
      : title;
    const quotedTitle = quoteTitleIfNeeded(pageTitle);
    const frontmatter = `---
title: ${quotedTitle}
---

`;
    content = frontmatter + content;
    needsUpdate = true;
  }
  
  if (needsUpdate) {
    fs.writeFileSync(filePath, content);
    console.log(`✓ ${pageTitle ? 'Updated' : 'Added'} frontmatter to ${path.basename(filePath)}`);
  }
}

// Function to process all markdown files in a directory
function processDirectory(dir) {
  const files = fs.readdirSync(dir);
  
  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);
    
    if (stat.isDirectory()) {
      processDirectory(filePath);
    } else if (file.endsWith('.md')) {
      addFrontmatter(filePath);
    }
  });
}

// Process docs directory
const docsDir = path.join(__dirname, '..', 'src', 'content', 'docs');

if (fs.existsSync(docsDir)) {
  console.log('Adding frontmatter to markdown files...');
  processDirectory(docsDir);
  console.log('✓ Frontmatter added to all markdown files');
} else {
  console.error('Error: src/content/docs directory not found');
  process.exit(1);
}

