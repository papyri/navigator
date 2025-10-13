// Highlight search terms in bibliography results
(function() {
  'use strict';

  // Get search query from URL parameters
  const urlParams = new URLSearchParams(window.location.search);
  const searchQuery = urlParams.get('q');

  // Function to remove accents from text
  function removeAccents(str) {
    return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }

  // Function to escape regex special characters except *
  function escapeRegex(str) {
    return str.replace(/[.+?^${}()|[\]\\]/g, '\\$&');
  }

  // Convert wildcards to regex pattern
  function wildcardToRegex(pattern) {
    const escaped = escapeRegex(pattern);
    return escaped.replace(/\\\*/g, '.*?');
  }

  // Function to highlight matches
  function highlightMatches() {
    const resultLinks = document.querySelectorAll('.result-record a');

    if (!searchQuery || searchQuery.trim() === '') return;

    // Split query into terms (handle quoted phrases and individual words)
    const terms = [];
    const quotedTerms = searchQuery.match(/"[^"]+"/g) || [];
    let remainingQuery = searchQuery;

    // Extract quoted terms
    quotedTerms.forEach(function(term) {
      terms.push(term.replace(/"/g, ''));
      remainingQuery = remainingQuery.replace(term, '');
    });

    // Extract individual words from remaining query
    const words = remainingQuery.trim().split(/\s+/).filter(function(word) {
      return word.length > 0;
    });

    // Process words to handle colon-separated terms like "author:Hombert"
    words.forEach(function(word) {
      if (word.includes(':')) {
        // Split on colon and take the part after the colon
        const colonParts = word.split(':');
        if (colonParts.length > 1 && colonParts[colonParts.length - 1].trim() !== '') {
          terms.push(colonParts[colonParts.length - 1].trim());
        }
      } else {
        terms.push(word);
      }
    });

    resultLinks.forEach(function(link) {
      // Work with HTML content to preserve existing tags
      let htmlContent = link.innerHTML;
      const originalTextContent = link.textContent;

      terms.forEach(function(term) {
        if (term.trim() === '') return;

        // Check if the original term contains wildcards
        const hasWildcards = term.indexOf('*') !== -1;

        // Create regex pattern for the term (with wildcard support)
        const exactPattern = wildcardToRegex(removeAccents(term.toLowerCase()));
        let exactRegex;

        if (hasWildcards) {
          // For wildcard terms, don't enforce word boundaries
          exactRegex = new RegExp('(' + exactPattern + ')', 'gi');
        } else {
          // For exact terms, use word boundaries to avoid partial matches
          exactRegex = new RegExp('\\b(' + exactPattern + ')\\b', 'gi');
        }

        // Test if this matches in the normalized text content
        const normalizedForTest = removeAccents(originalTextContent.toLowerCase());
        if (exactRegex.test(normalizedForTest)) {
          // Reset regex for actual replacement
          exactRegex.lastIndex = 0;

          // Create a regex to match the term in HTML while avoiding existing tags
          // This regex looks for the term outside of HTML tags
          const htmlSafePattern = exactPattern.replace(/\(/g, '\\(').replace(/\)/g, '\\)');

          // Create regex that matches the term only in text content, not in HTML tags
          // Negative lookahead to avoid matching inside HTML tags
          let htmlRegex;
          if (hasWildcards) {
            htmlRegex = new RegExp('(?![^<]*>)(' + htmlSafePattern + ')(?![^<]*</)', 'gi');
          } else {
            htmlRegex = new RegExp('(?![^<]*>)\\b(' + htmlSafePattern + ')\\b(?![^<]*</)', 'gi');
          }

          // Apply highlighting to HTML content
          htmlContent = htmlContent.replace(htmlRegex, '<mark>$1</mark>');
        }
      });

      // Update the link's HTML if highlighting was applied
      if (htmlContent !== link.innerHTML) {
        link.innerHTML = htmlContent;
      }
    });
  }

  // Function to focus keyword search when no parameters exist
  function focusKeywordSearch() {
    const keywordSearch = document.getElementById('keyword');
    if (keywordSearch) {
      const urlParams = new URLSearchParams(window.location.search);
      if (!urlParams.toString()) {
        keywordSearch.focus();
      }
    }
  }

  // Function to manage skip-to links visibility
  function manageSkipLinks() {
    const biblioResults = document.getElementById('biblio-results');
    const skipLink = document.querySelector('#skip-links a[href="#biblio-results"]');

    if (skipLink && !biblioResults) {
      skipLink.classList.add('d-none');
    }
  }

  // Function to initialize all functionality
  function initialize() {
    highlightMatches();
    focusKeywordSearch();
    manageSkipLinks();
  }

  // Run initialization when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initialize);
  } else {
    // DOM is already ready
    initialize();
  }

})();
