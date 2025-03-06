import React from 'react';
import { Box, Button, Container, Typography, Paper, Stack } from '@mui/material';
import config from '../config/config';

function LandingPage() {
  return (
    <Container maxWidth="md" sx={{ mt: 8 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Typography variant="h3" component="h1" gutterBottom align="center">
          Array Banking
        </Typography>
        
        <Typography variant="h5" gutterBottom align="center" color="text.secondary">
          Choose Your Preferred Interface
        </Typography>
        
        <Box sx={{ mt: 4 }}>
          <Stack spacing={3} direction={{ xs: 'column', sm: 'row' }} justifyContent="center">
            <Button 
              variant="contained" 
              size="large" 
              href="/spa/login" 
              sx={{ minWidth: 200, py: 1.5 }}
            >
              React SPA Version
            </Button>
            
            <Button 
              variant="outlined" 
              size="large" 
              href={`${config.ssrUrl}/login`} 
              sx={{ minWidth: 200, py: 1.5 }}
            >
              Server Rendered Version (HTMX)
            </Button>
          </Stack>
        </Box>
      </Paper>
    </Container>
  );
}

export default LandingPage;
