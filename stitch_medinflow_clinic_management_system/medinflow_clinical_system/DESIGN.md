---
name: MedInFlow Clinical System
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#434655'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#006c49'
  on-secondary: '#ffffff'
  secondary-container: '#6cf8bb'
  on-secondary-container: '#00714d'
  tertiary: '#943700'
  on-tertiary: '#ffffff'
  tertiary-container: '#bc4800'
  on-tertiary-container: '#ffede6'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffdbcd'
  tertiary-fixed-dim: '#ffb596'
  on-tertiary-fixed: '#360f00'
  on-tertiary-fixed-variant: '#7d2d00'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  title-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-caps:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  xs: 0.25rem
  sm: 0.5rem
  md: 1rem
  lg: 1.5rem
  xl: 2rem
  container-max: 1280px
  sidebar-width: 260px
  gutter: 24px
---

## Brand & Style

This design system is engineered for high-stakes medical environments where clarity, speed, and reliability are paramount. The aesthetic follows a **Corporate / Modern** direction, utilizing a rigorous grid and high-fidelity detailing to foster a sense of institutional trust and clinical precision.

The visual language balances the cold efficiency of medical software with a user-centric "High-Fidelity" approach—incorporating subtle gradients, refined micro-interactions, and a clear information hierarchy. The interface prioritizes data density without sacrificing legibility, ensuring healthcare professionals can process complex patient records and schedules with minimal cognitive load.

## Colors

The palette is anchored by **Medical Professional Blue (#2563EB)**, a color chosen for its association with authority and calm. 

- **Primary:** Used for the main action buttons, active navigation states, and primary brand touchpoints.
- **Success (Green):** Reserved for "CONFIRMADA" statuses and positive completion states.
- **Warning (Orange):** Utilized for "SIN_CONFIRMAR" statuses and cautionary system alerts.
- **Danger (Red):** Dedicated to "CANCELADA" statuses and destructive actions.
- **Neutral (Grays):** A sophisticated scale of Slate grays handles the "NO_ASISTIO" status, borders, and secondary text, maintaining a clean architectural feel.

The background uses a very slight cool tint (#F8FAFC) to reduce screen glare during long shifts, while surfaces remain pure white to define clear content boundaries.

## Typography

This design system utilizes **Inter** for its exceptional legibility and systematic structure. The type scale is optimized for data-heavy desktop environments.

- **Headlines:** Use tighter letter-spacing and heavier weights to provide clear section anchoring.
- **Body Text:** Set with generous line height (1.5x) to ensure patient notes and medical records are readable at a glance.
- **Labels:** Small caps are used sparingly for table headers and status badges to differentiate metadata from interactive content.
- **Data Mono:** Where numerical patient IDs or clinical values appear, consider a tabular figures stylistic set to ensure alignment in data tables.

## Layout & Spacing

The system employs a **Fixed Grid** philosophy for the main content area, centered within a 1280px container to ensure a consistent experience across desktop monitors.

- **Sidebar:** A fixed 260px left-hand navigation allows for persistent access to clinical modules.
- **Grid:** A 12-column layout with 24px gutters provides the framework for metric cards and data tables.
- **Rhythm:** An 8pt linear scale is used for all internal component spacing, ensuring a disciplined and mathematical visual rhythm.
- **Safe Areas:** 32px page margins are maintained to provide visual breathing room in dense information views.

## Elevation & Depth

Hierarchy is established through **Tonal Layers** and **Ambient Shadows**. This design system avoids heavy skeuomorphism in favor of subtle depth cues:

- **Level 0 (Background):** #F8FAFC. The canvas layer.
- **Level 1 (Cards/Sidebar):** White surface with a 1px border (#E2E8F0) and a very soft, diffused shadow (0 1px 3px rgba(0,0,0,0.05)).
- **Level 2 (Dropdowns/Modals):** High-diffusion shadows (0 10px 25px rgba(0,0,0,0.1)) to lift functional elements above the clinical data.
- **Overlay:** Modals utilize a high-opacity dark overlay (#0F172A at 70% opacity) to completely isolate the user's focus during critical entries.

## Shapes

The shape language is **Soft (0.25rem)**, emphasizing a professional and organized character. 

- **Components:** Buttons and input fields use the base 4px (0.25rem) radius.
- **Cards:** Metric cards and data containers use `rounded-lg` (8px) to soften the large surface areas.
- **Status Badges:** Use a "Pill" shape (full rounding) to clearly distinguish them from interactive buttons.
- **Chat Bubbles:** The WhatsApp/ChatGPT interface uses a larger 12px radius, with the "tail" corner remaining sharp to indicate the speaker.

## Components

### Sidebar Navigation
A dark-themed or high-contrast light sidebar with vertical icons. Active states use a "Primary Blue" left-accent border (4px) and a subtle background tint.

### Data Tables
- **Header:** Light gray background (#F1F5F9), uppercase labels.
- **Rows:** Hover state triggers a subtle color shift (#F8FAFC). 
- **Pagination:** Clean, numerical buttons with "Previous" and "Next" text labels.

### Status Badges
- **CONFIRMADA:** Green background (10% opacity) with Green text.
- **SIN_CONFIRMAR:** Yellow background (10% opacity) with Dark Yellow text.
- **CANCELADA:** Red background (10% opacity) with Red text.
- **NO_ASISTIO:** Gray background (10% opacity) with Slate text.

### Metric Cards
White surfaces with a top-accent line in Primary Blue. Large "Display" font size for the primary metric value and small "Label" font for the description.

### Chat Interface
A split-pane view. The left side lists active "Conversations," while the right side is the message thread. Input fields are pinned to the bottom with a subtle shadow to separate them from the scrolling content.

### Modals
Centered on screen, max-width of 560px for standard forms. Headers include a "Close" icon and a clear Title.